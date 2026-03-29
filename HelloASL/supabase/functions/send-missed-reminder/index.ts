import { createClient } from "jsr:@supabase/supabase-js@2";

const SUPABASE_URL = Deno.env.get("SUPABASE_URL");
const SUPABASE_ANON_KEY = Deno.env.get("SUPABASE_ANON_KEY");
const SUPABASE_SERVICE_ROLE_KEY = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");
const FUNCTIONS_BASE_URL =
    Deno.env.get("FUNCTIONS_BASE_URL") ??
    "https://dbdwlwyemwjivrrvuzjz.supabase.co/functions/v1";
const INTERNAL_FUNCTION_KEY = Deno.env.get("INTERNAL_FUNCTION_KEY");

if (
    !SUPABASE_URL ||
    !SUPABASE_ANON_KEY ||
    !SUPABASE_SERVICE_ROLE_KEY ||
    !INTERNAL_FUNCTION_KEY
) {
  throw new Error("Send-missed-reminder: Missing required environment variables");
}

type HistoryRow = {
  sent_date: string;
};

Deno.serve(async (req) => {
  try {
    if (req.method !== "POST") {
      return Response.json(
          { error: "Send-missed-reminder: Method not allowed" },
          { status: 405 }
      );
    }

    const authHeader = req.headers.get("Authorization");
    if (!authHeader) {
      return Response.json(
          { error: "Send-missed-reminder: Missing Authorization header" },
          { status: 401 }
      );
    }

    const userClient = createClient(SUPABASE_URL, SUPABASE_ANON_KEY, {
      global: {
        headers: {
          Authorization: authHeader,
        },
      },
    });

    const {
      data: { user },
      error: userError,
    } = await userClient.auth.getUser();

    if (userError || !user) {
      return Response.json(
          { error: "Send-missed-reminder: Unauthorized" },
          { status: 401 }
      );
    }

    const adminClient = createClient(SUPABASE_URL, SUPABASE_SERVICE_ROLE_KEY);

    const now = new Date();

    const today = new Intl.DateTimeFormat("en-CA", {
      timeZone: "America/Toronto",
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
    }).format(now);

    const currentHour = Number(
        new Intl.DateTimeFormat("en-US", {
          timeZone: "America/Toronto",
          hour: "2-digit",
          hour12: false,
        }).format(now)
    );

    const reminderHour = 9;

    if (currentHour < reminderHour) {
      return Response.json({
        success: true,
        skipped: true,
        reason: "Too early",
        currentHour,
        today,
        timezone: "America/Toronto",
      });
    }

    const { data: history, error: historyError } = await adminClient
        .from("NotificationHistory")
        .select("sent_date")
        .eq("user_id", user.id)
        .eq("notification_type", "daily_reminder")
        .maybeSingle<HistoryRow>();

    if (historyError) {
      throw new Error(
          `Send-missed-reminder: Failed to check notification history: ${historyError.message}`
      );
    }

    if (history?.sent_date === today) {
      return Response.json({
        success: true,
        skipped: true,
        reason: "Already sent today",
      });
    }

    const sendRes = await fetch(`${FUNCTIONS_BASE_URL}/send-fcm`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "x-internal-key": INTERNAL_FUNCTION_KEY,
      },
      body: JSON.stringify({
        title: "HelloASL",
        body: "Time to practice ASL today 👋",
        type: "daily_reminder",
        userId: user.id,
      }),
    });

    const rawText = await sendRes.text();

    let parsed: unknown;
    try {
      parsed = JSON.parse(rawText);
    } catch {
      parsed = { raw: rawText };
    }

    const result = parsed as {
      success?: boolean;
      message?: string;
      sent?: number;
      failed?: number;
      results?: unknown[];
    };

    const sentCount = typeof result?.sent === "number" ? result.sent : 0;

    if (!sendRes.ok) {
      return Response.json(
          {
            success: false,
            status: sendRes.status,
            result: parsed,
          },
          { status: sendRes.status }
      );
    }

    if (sentCount <= 0) {
      return Response.json(
          {
            success: true,
            status: sendRes.status,
            skipped: true,
            reason: "No notification delivered",
            result: parsed,
          },
          { status: 200 }
      );
    }

    const { error: upsertError } = await adminClient
        .from("NotificationHistory")
        .upsert(
            {
              user_id: user.id,
              notification_type: "daily_reminder",
              sent_date: today,
            },
            {
              onConflict: "user_id,notification_type",
            }
        );

    if (upsertError) {
      throw new Error(
          `Send-missed-reminder: Failed to update notification history: ${upsertError.message}`
      );
    }

    return Response.json(
        {
          success: true,
          status: sendRes.status,
          result: parsed,
        },
        { status: 200 }
    );
  } catch (e) {
    return Response.json(
        { error: e instanceof Error ? e.message : String(e) },
        { status: 500 }
    );
  }
});