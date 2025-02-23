import { transcribe } from '$lib/grpc/transcribe';

const ETranscribeStatus = {
    PROCESSING: "processing",
    CHUNK: "chunk",
    COMPLETED: "completed",
    ERROR: "error"
} as const;

export type TranscribeStatus = typeof ETranscribeStatus[keyof typeof ETranscribeStatus];
export type TranscribeResponse = {
    status: TranscribeStatus;
    result?: string;
}

export async function POST({ request }) {
    const formData = await request.formData();
    const file = formData.get('file');

    if (!file || !(file instanceof Blob)) {
        return new Response('No file uploaded', { status: 400 });
    }

    const reader = file.stream().getReader();
    const stream = new ReadableStream({
        async start(controller) {
            const encoder = new TextEncoder();

            try {
                // Send initial processing message
                const processingMessage: TranscribeResponse = { status: ETranscribeStatus.PROCESSING };
                controller.enqueue(encoder.encode(JSON.stringify(processingMessage) + "\n"));

                await transcribe(reader, (text: string) => {
                    const chunk: TranscribeResponse = { status: ETranscribeStatus.CHUNK, result: text };
                    controller.enqueue(encoder.encode(JSON.stringify(chunk) + "\n"));
                });

                // Send the final result
                const completedMessage: TranscribeResponse = { status: ETranscribeStatus.COMPLETED };
                controller.enqueue(encoder.encode(JSON.stringify(completedMessage) + "\n"));
                controller.close();
            } catch(error) {
                const errorMessage: TranscribeResponse = {
                    status: ETranscribeStatus.ERROR,
                    result: error instanceof Error ? error.message : String(error)
                };

                controller.enqueue(encoder.encode(JSON.stringify(errorMessage) + "\n"));
                controller.close();
            }
        }
    });

    return new Response(stream, {
        headers: {
            'Content-Type': 'application/json',
            'Transfer-Encoding': 'chunked',
            'Connection': 'keep-alive'
        }
    });
}
