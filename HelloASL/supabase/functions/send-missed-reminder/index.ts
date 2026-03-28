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
      return Response.json({ error: "Send-missed-reminder: Method not allowed" }, { status: 405 });
    }

    const authHeader = req.headers.get("Authorization");
    if (!authHeader) {
      return Response.json(
          { error: "Send-missed-reminder: Missing Authorization header" },
          { status: 401 }
      );
    }

    // Client scoped to the signed-in user
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
      return Response.json({ error: "Send-missed-reminder: Unauthorized" }, { status: 401 });
    }

    // Service-role client for DB reads
    const adminClient = createClient(SUPABASE_URL, SUPABASE_SERVICE_ROLE_KEY);

    const now = new Date();

    // Adjust this to your desired reminder hour
    const reminderHour = 9;

    // If you want a specific timezone, replace with your own logic later.
    const currentHour = now.getHours();
    if (currentHour < reminderHour) {
      return Response.json({
        success: true,
        skipped: true,
        reason: "Too early",
      });
    }

    const today = now.toISOString().slice(0, 10);

    const { data: history, error: historyError } = await adminClient
        .from("NotificationHistory")
        .select("sent_date")
        .eq("user_id", user.id)
        .eq("notification_type", "daily_reminder")
        .maybeSingle<HistoryRow>();

    if (historyError) {
      throw new Error(`Send-missed-reminder: Failed to check notification history: ${historyError.message}`);
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

    return Response.json(
        {
          success: sendRes.ok,
          status: sendRes.status,
          result: parsed,
        },
        { status: sendRes.ok ? 200 : sendRes.status }
    );
  } catch (e) {
    return Response.json(
        { error: e instanceof Error ? e.message : String(e) },
        { status: 500 }
    );
  }
});