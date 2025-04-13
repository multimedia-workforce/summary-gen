import {json} from '@sveltejs/kit';

const PERSISTENCE_URL = process.env.PERSISTENCE_URL ?? 'http://localhost:8081';

export async function GET({locals}) {
    if (!locals.user || !locals.jwt) {
        return json({error: 'Unauthorized'}, {status: 401});
    }

    const userId = locals.user.id;

    const res = await fetch(`${PERSISTENCE_URL}/smartSessions?userId=${userId}`, {
        headers: {
            Authorization: `Bearer ${locals.jwt}`
        }
    });

    if (!res.ok) {
        const error = await res.text();
        return json({error: error || 'Failed to fetch smart sessions'}, {status: res.status});
    }

    const data = await res.json();
    return json(data);
}
