import { models } from '@/grpc/summarize';
import { json } from '@sveltejs/kit';

/**
 * Handles a GET request to retrieve the available models
 */
export async function GET() {
    return new Promise((resolve) => {
        models()
            .then((response) => {
                resolve(json({ models: response }))
            })
            .catch(() => resolve(json({ models: [] })));
    });
}
