import { transcribe } from '$lib/transcribe';

export async function POST({ request }) {
    const stream = new ReadableStream({
        start(controller) {
            const callback = (text: string) => {
                controller.enqueue(`${text}\n`);
            };

            request.arrayBuffer().then((buffer) => {
                transcribe(Buffer.from(buffer), callback);
            }).catch((error) => {
                console.error(error);
            });
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
