# Use Alpine as a parent image
FROM alpine:latest

# Install essential build tools and dependencies
RUN apk add --no-cache \
    make \
    clang \
    cmake \
    ninja \
    git \
    ffmpeg \
    ffmpeg-dev \
    grpc-dev \
	curl-dev \
    protobuf \
    protobuf-dev \
    ca-certificates

# Set up a working directory
WORKDIR /app

# Copy the backend files
COPY ./backend .
COPY ./proto ../proto

# Download tiny model if it isn't already present
RUN mkdir -p ./models && \
    [ -f ./models/ggml-tiny.bin ] || wget -P ./models https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-tiny.bin

# Create a build directory and build the project
RUN cmake --preset=lin-64-release
RUN cmake --build --preset=lin-64-release

# Run service
CMD ["./build/lin-64-release/summary-backend", "./models/ggml-tiny.bin", "50051"]
