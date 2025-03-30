# Use the official Node.js image
FROM node:20-alpine

# Install essential build tools and dependencies
RUN apk add --no-cache \
    protobuf \
    protobuf-dev

# Set the working directory
WORKDIR /app

# Copy the package.json and package-lock.json
COPY ./frontend/package*.json ./

# Install the dependencies
RUN npm ci

# Copy the rest of the application code
COPY ./frontend .
COPY ./proto ../proto

# Build the app for production
RUN npm run build

# Serve the app
CMD [ "node", "build" ]
