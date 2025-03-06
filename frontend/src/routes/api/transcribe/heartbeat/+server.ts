import { heartbeat } from "@/grpc/transcribe";
import { json } from "@sveltejs/kit";

/**
 * Handles a GET request to check server health.
 */
export async function GET() {
    return new Promise((resolve, reject) => {
        heartbeat().then((isAlive) => {
            resolve(json({ status: isAlive ? 'OK' : 'DOWN' }));
        }).catch(reject);
    });
}
