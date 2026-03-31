Deno.serve(async () => {
    try {
        const anonKey = Deno.env.get("ANON_KEY");
        const functionsBaseUrl =
            Deno.env.get("FUNCTIONS_BASE_URL") ??
            "https://dbdwlwyemwjivrrvuzjz.supabase.co/functions/v1";
        const internalKey = Deno.env.get("INTERNAL_FUNCTION_KEY");

        if (!anonKey) {
            return Response.json(
                { error: "Missing ANON_KEY secret" },
                { status: 500 }
            );
        }

        if (!internalKey) {
            return Response.json(
                { error: "Missing INTERNAL_FUNCTION_KEY secret" },
                { status: 500 }
            );
        }

        const res = await fetch(`${functionsBaseUrl}/send-fcm`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${anonKey}`,
                "x-internal-key": internalKey,
            },
            body: JSON.stringify({
                title: "HelloASL",
                body: "Time to practice ASL today 👋",
                type: "daily_reminder",
            }),
        });

        const rawText = await res.text();

        let parsed: unknown;
        try {
            parsed = JSON.parse(rawText);
        } catch {
            parsed = { raw: rawText };
        }

        return Response.json(
            {
                success: res.ok,
                status: res.status,
                result: parsed,
            },
            { status: res.ok ? 200 : res.status }
        );
    } catch (e) {
        return Response.json(
            { error: e instanceof Error ? e.message : String(e) },
            { status: 500 }
        );
    }
});