# Meeting Summary

This project is an automatic meeting summarization tool that transcribes and summarizes video/audio recordings using [whisper.cpp](https://github.com/ggerganov/whisper.cpp). It processes media files using `ffmpeg`, transcribes them with whisper, and provides a gRPC-based API. The frontend interface is built using SvelteKit.

## Features
- Transcribe audio and video files into text
- Generate meeting summaries from transcripts
- gRPC-based communication between the backend and frontend
- SvelteKit-based web interface for user interaction

## Prerequisites
Ensure you have the following dependencies installed before building and running the project:

### System Dependencies

 - [CMake](https://cmake.org)
 - [LLVM](https://llvm.org)
 - [Ninja](https://ninja-build.org)
 - [VSCode](https://code.visualstudio.com)
 - [nodejs](https://nodejs.org/)
 - [ffmpeg libraries](https://ffmpeg.org)
 - [protobuf](https://protobuf.dev)
 - [grpc](https://grpc.io)

### Additional Requirements
Download a Whisper model before running the project. Models are available at [Whisper.cpp models](https://github.com/ggerganov/whisper.cpp#usage). Example:
```bash
cd core/models
wget https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-tiny.bin
```

## Installation
Clone the repository and navigate to the project directory:
```bash
git clone https://github.com/yourusername/automatic-meeting-summarizer.git
cd automatic-meeting-summarizer
```

### Running the Backend
```bash
cd core
cmake --preset=<os>-64-release
cmake --preset=<os>-64-release --build
./build/<os>-64-release/meeting-summary models/ggml-tiny.bin 50051
```

### Setting Up the Frontend
```bash
cd frontend
npm install
npm run dev
```
The frontend should now be available at `http://localhost:5173`.

## Usage
1. Upload an audio or video file via the web interface.
2. The backend processes the media file using `ffmpeg`.
3. `whisper.cpp` transcribes the audio.
4. The transcription is streamed to the web interface
