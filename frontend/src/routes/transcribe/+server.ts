import { transcribe } from '$lib/transcribe';

export async function POST({ request }) {
    const formData = await request.formData();
    const file = formData.get('file');

    if (!file || !(file instanceof Blob)) {
        return new Response('No file uploaded', { status: 400 });
    }

    const reader = file.stream().getReader();
    const stream = new ReadableStream({
        async start(controller) {
            const callback = (text: string) => {
                controller.enqueue(text);
            };

            try {
                await transcribe(reader, callback);
            } catch (error) {
                controller.error(error);
            } finally {
                controller.close();
            }
        }
    });

    return new Response(stream, {
        headers: {
            'Content-Type': 'text/plain',
            'Transfer-Encoding': 'chunked',
            'Connection': 'keep-alive'
        }
    });
}

// Handle CORS preflight
export async function OPTIONS() {
    return new Response(null, {
        headers: {
            'Access-Control-Allow-Origin': '*',
            'Access-Control-Allow-Methods': 'POST, OPTIONS',
            'Access-Control-Allow-Headers': 'Content-Type'
        }
    });
}
