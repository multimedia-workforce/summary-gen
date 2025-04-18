import {json} from '@sveltejs/kit';

const ANALYTICS_URL = process.env.ANALYTICS_URL ?? 'http://localhost:8082';

export async function GET({locals}) {
    if (!locals.user || !locals.jwt) {
        return json({error: 'Unauthorized'}, {status: 401});
    }

    const res = await fetch(`${ANALYTICS_URL}/metrics`, {
        headers: {
            Authorization: `Bearer ${locals.jwt}`,
            "Content-Type": "application/json"
        }
    });

    if (!res.ok) {
        const error = await res.text();
        return json({error: error || 'Failed to fetch analytics'}, {status: res.status});
    }

    const data = await res.json();
    return json(data);
}

export async function POST({request, locals}) {
    if (!locals.user || !locals.jwt) {
        return json({error: 'Unauthorized'}, {status: 401});
    }

    const res = await fetch(`${ANALYTICS_URL}/metrics`, {
        method: 'POST',
        headers: {
            Authorization: `Bearer ${locals.jwt}`,
            "Content-Type": "application/json"
        },
        body: JSON.stringify(await request.json())
    });

    if (!res.ok) {
        const error = await res.text();
        return json({error: error || 'Failed to fetch metrics'}, {status: res.status});
    }

    const data = await res.json();
    return json(data);
}