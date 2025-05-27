import { type Settings } from '$lib/grpc/gen/settings';
import type { RequestHandler } from "@sveltejs/kit";

const settingsStore = new Map<string, Settings>();

/**
 * GET /api/settings
 * Returns the user-specific custom prompt
 */
export const GET: RequestHandler = async ({ locals }) => {
    if (!locals.user) {
        return new Response('Unauthorized', { status: 401 });
    }

    const settings = settingsStore.get(locals.user.id) || { prompt: "Summarize the following transcript." };
    return new Response(JSON.stringify(settings), {
        status: 200,
        headers: { "Content-Type": "application/json" }
    });
};

/**
 * POST /api/settings
 * Updates the user-specific custom prompt
 */
export const POST: RequestHandler = async ({ locals, request }) => {
    if (!locals.user) {
        return new Response('Unauthorized', { status: 401 });
    }

    const client = await request.json();

    const settings: Settings = {
        userId: locals.user.id,
        prompt: client.prompt || 'Summarize the following transcript.',
    };

    settingsStore.set(locals.user.id, settings);

    return new Response(null, { status: 204 });
};