// src/routes/api/persistence/[id]/+server.ts
import {json} from '@sveltejs/kit';

const PERSISTENCE_URL = process.env.PERSISTENCE_URL ?? 'http://localhost:8081';

export async function GET({params, locals}) {
    if (!locals.user || !locals.jwt) {
        return json({error: 'Unauthorized'}, {status: 401});
    }

    const id = params.id;
    const res = await fetch(`${PERSISTENCE_URL}/smartSessions/${id}`, {
        headers: {
            Authorization: `Bearer ${locals.jwt}`
        }
    });

    if (!res.ok) {
        const error = await res.text();
        return json({error: error || 'Failed to fetch smart session'}, {status: res.status});
    }

    const data = await res.json();
    return json(data);
}

/**
 * Handler for DELETE HTTP requests to delete specific smart sessions
 * @param params The parameters of the request
 * @param locals The locals of the handler (storage)
 * @return An HTTP response
 */
export async function DELETE({ params, locals }) {
    if (!locals.user || !locals.jwt) {
        return json({ error: 'Unauthorized' }, { status: 401 });
    }

    const id = params.id;
    const res = await fetch(`${PERSISTENCE_URL}/smartSessions/${id}`, {
        method: 'DELETE',
        headers: {
            Authorization: `Bearer ${locals.jwt}`
        }
    });

    if (!res.ok) {
        const error = await res.text();
        return json({ error: error || 'Failed to delete smart session' }, { status: res.status });
    }

    return new Response(null, { status: 204 });
}
