
const PERSISTENCE_URL = process.env.PERSISTENCE_URL ?? 'http://localhost:8081';

/**
 * Handles a GET request to retrieve smart sessions
 */
export async function GET() {
    return fetch(`${PERSISTENCE_URL}/smartSessions?userId=1cc8ed0d-191a-4aeb-8579-3d04e4c8d6b8`);
}
