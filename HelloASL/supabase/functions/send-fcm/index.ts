import { createClient } from "jsr:@supabase/supabase-js@2";

const FIREBASE_PROJECT_ID = Deno.env.get("FIREBASE_PROJECT_ID");
const FIREBASE_CLIENT_EMAIL = Deno.env.get("FIREBASE_CLIENT_EMAIL");
const FIREBASE_PRIVATE_KEY = Deno.env
    .get("FIREBASE_PRIVATE_KEY")
    ?.replace(/\\n/g, "\n");

const SUPABASE_URL = Deno.env.get("SUPABASE_URL");
const SUPABASE_SERVICE_ROLE_KEY = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");

if (
    !FIREBASE_PROJECT_ID ||
    !FIREBASE_CLIENT_EMAIL ||
    !FIREBASE_PRIVATE_KEY ||
    !SUPABASE_URL ||
    !SUPABASE_SERVICE_ROLE_KEY
) {
  throw new Error("Missing required environment variables");
}

const supabase = createClient(SUPABASE_URL, SUPABASE_SERVICE_ROLE_KEY);

type GoogleJwtClaims = {
  iss: string;
  scope: string;
  aud: string;
  exp: number;
  iat: number;
};

type SendRequest = {
  title: string;
  body: string;
  type?: string;
  userId?: string;
};

type DeviceTokenRow = {
  user_id: string;
  token: string;
};

type SendResult = {
  ok: boolean;
  status: number;
  token: string;
  data: unknown;
};

function toBase64Url(input: Uint8Array): string {
  let binary = "";
  for (const byte of input) binary += String.fromCharCode(byte);
  return btoa(binary)
      .replace(/\+/g, "-")
      .replace(/\//g, "_")
      .replace(/=+$/g, "");
}

async function createSignedJwt(): Promise<string> {
  const now = Math.floor(Date.now() / 1000);

  const header = {
    alg: "RS256",
    typ: "JWT",
  };

  const claims: GoogleJwtClaims = {
    iss: FIREBASE_CLIENT_EMAIL!,
    scope: "https://www.googleapis.com/auth/firebase.messaging",
    aud: "https://oauth2.googleapis.com/token",
    iat: now,
    exp: now + 3600,
  };

  const encoder = new TextEncoder();
  const encodedHeader = toBase64Url(
      encoder.encode(JSON.stringify(header))
  );
  const encodedClaims = toBase64Url(
      encoder.encode(JSON.stringify(claims))
  );
  const unsignedJwt = `${encodedHeader}.${encodedClaims}`;

  const pem = FIREBASE_PRIVATE_KEY!
      .replace("-----BEGIN PRIVATE KEY-----", "")
      .replace("-----END PRIVATE KEY-----", "")
      .replace(/\s+/g, "");

  const binaryDer = Uint8Array.from(atob(pem), (c) => c.charCodeAt(0));

  const cryptoKey = await crypto.subtle.importKey(
      "pkcs8",
      binaryDer.buffer,
      {
        name: "RSASSA-PKCS1-v1_5",
        hash: "SHA-256",
      },
      false,
      ["sign"]
  );

  const signature = await crypto.subtle.sign(
      "RSASSA-PKCS1-v1_5",
      cryptoKey,
      encoder.encode(unsignedJwt)
  );

  const encodedSignature = toBase64Url(new Uint8Array(signature));
  return `${unsignedJwt}.${encodedSignature}`;
}

async function getAccessToken(): Promise<string> {
  const jwt = await createSignedJwt();

  const res = await fetch("https://oauth2.googleapis.com/token", {
    method: "POST",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded",
    },
    body: new URLSearchParams({
      grant_type: "urn:ietf:params:oauth:grant-type:jwt-bearer",
      assertion: jwt,
    }),
  });

  const data = await res.json();

  if (!res.ok) {
    throw new Error(
        `Failed to get Google access token: ${JSON.stringify(data)}`
    );
  }

  return data.access_token;
}

async function sendToToken(
    accessToken: string,
    token: string,
    title: string,
    body: string,
    type: string
): Promise<SendResult> {
  const res = await fetch(
      `https://fcm.googleapis.com/v1/projects/${FIREBASE_PROJECT_ID}/messages:send`,
      {
        method: "POST",
        headers: {
          "Authorization": `Bearer ${accessToken}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          message: {
            token,
            notification: {
              title,
              body,
            },
            data: {
              title,
              body,
              type,
            },
          },
        }),
      }
  );

  const data = await res.json();

  return {
    ok: res.ok,
    status: res.status,
    token,
    data,
  };
}

function isUnregisteredTokenError(data: unknown): boolean {
  const text = JSON.stringify(data);
  return (
      text.includes("UNREGISTERED") ||
      text.includes("registration-token-not-registered") ||
      text.includes("Requested entity was not found")
  );
}

async function deleteInvalidToken(token: string) {
  await supabase.from("DeviceTokens").delete().eq("token", token);
}

Deno.serve(async (req) => {
  console.log("Send-missed-reminder: send-fcm invoked");
  try {
    if (req.method !== "POST") {
      return new Response("Method not allowed", { status: 405 });
    }

    const internalApiKey = Deno.env.get("INTERNAL_FUNCTION_KEY");
    const providedKey = req.headers.get("x-internal-key");

    if (!internalApiKey || providedKey !== internalApiKey) {
      return Response.json({ error: "Unauthorized" }, { status: 401 });
    }

    const { title, body, type, userId }: SendRequest = await req.json();

    if (!title || !body) {
      return Response.json(
          { error: "title and body are required" },
          { status: 400 }
      );
    }

    let query = supabase
        .from("DeviceTokens")
        .select("user_id, token")
        .eq("platform", "android");

    if (userId) {
      query = query.eq("user_id", userId);
    }

    const { data: rows, error } = await query;

    if (error) {
      throw new Error(`Failed to load device tokens: ${error.message}`);
    }

    const tokenRows = ((rows ?? []) as DeviceTokenRow[]).filter(
        (row) => row.user_id && row.token
    );

    if (tokenRows.length === 0) {
      return Response.json({
        success: true,
        message: "No device tokens found",
        sent: 0,
        failed: 0,
        results: [],
      });
    }

    // Deduplicate by token, but keep user_id
    const uniqueTokenRows = Array.from(
        new Map(tokenRows.map((row) => [row.token, row])).values()
    );

    const accessToken = await getAccessToken();
    const notificationType = type ?? "general";

    const results: Array<SendResult & { user_id: string }> = [];
    const successfulUserIds = new Set<string>();

    for (const row of uniqueTokenRows) {
      const result = await sendToToken(
          accessToken,
          row.token,
          title,
          body,
          notificationType
      );

      if (!result.ok && isUnregisteredTokenError(result.data)) {
        await deleteInvalidToken(row.token);
      }

      if (result.ok) {
        successfulUserIds.add(row.user_id);
      }

      results.push({
        ...result,
        user_id: row.user_id,
      });
    }

    const successCount = results.filter((r) => r.ok).length;
    const failedCount = results.filter((r) => !r.ok).length;

    if (notificationType === "daily_reminder" && successfulUserIds.size > 0) {
      const today = new Date().toISOString().slice(0, 10);
      const nowIso = new Date().toISOString();

      const historyRows = Array.from(successfulUserIds).map((uid) => ({
        user_id: uid,
        notification_type: notificationType,
        sent_date: today,
        updated_at: nowIso,
      }));

      const { error: historyError } = await supabase
          .from("NotificationHistory")
          .upsert(historyRows, {
            onConflict: "user_id,notification_type",
          });

      if (historyError) {
        console.error(
            "Failed to record notification history:",
            historyError.message
        );
      }
    }

    return Response.json({
      success: true,
      sent: successCount,
      failed: failedCount,
      results,
    });
  } catch (e) {
    return Response.json(
        { error: e instanceof Error ? e.message : String(e) },
        { status: 500 }
    );
  }
});