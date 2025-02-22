import { heartbeat } from "$lib/transcribe";
import { json } from "@sveltejs/kit";

/**
 * Handles a GET request to check server health.
 */
export async function GET() {
    const isAlive = await heartbeat();
    return json({ status: isAlive ? 'OK' : 'DOWN' });
}
