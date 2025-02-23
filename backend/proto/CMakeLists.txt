# Protobuf cmake configuration

# Copy protobuf files from shared folder
file(GLOB PROTO_SOURCES_ORIGINAL "${CMAKE_SOURCE_DIR}/../proto/*.proto")
file(COPY ${PROTO_SOURCES_ORIGINAL} DESTINATION ${CMAKE_CURRENT_SOURCE_DIR})
file(GLOB PROTO_SOURCES
        "${CMAKE_CURRENT_SOURCE_DIR}/*.proto"
)

set(PROTO_LIBRARY_NAME "${PROJECT_NAME}-proto")
add_library(${PROTO_LIBRARY_NAME} OBJECT ${PROTO_SOURCES})

target_link_libraries(${PROTO_LIBRARY_NAME} PUBLIC protobuf::libprotobuf gRPC::grpc++_unsecure)

set(PROTO_BINARY_DIR "${CMAKE_CURRENT_BINARY_DIR}/generated")
set(PROTO_IMPORT_DIRS "${CMAKE_CURRENT_LIST_DIR}")

protobuf_generate(
        TARGET ${PROTO_LIBRARY_NAME}
        OUT_VAR PROTO_GENERATED_FILES
        IMPORT_DIRS ${PROTO_IMPORT_DIRS}
        PROTOC_OUT_DIR ${PROTO_BINARY_DIR}
)

set_source_files_properties(${PROTO_GENERATED_FILES} PROPERTIES SKIP_UNITY_BUILD_INCLUSION on)

protobuf_generate(
        TARGET ${PROTO_LIBRARY_NAME}
        OUT_VAR PROTO_GENERATED_FILES
        LANGUAGE grpc
        GENERATE_EXTENSIONS .grpc.pb.h .grpc.pb.cc
        PLUGIN "protoc-gen-grpc=\$<TARGET_FILE:gRPC::grpc_cpp_plugin>"
        # PLUGIN_OPTIONS "generate_mock_code=true"
        IMPORT_DIRS ${PROTO_IMPORT_DIRS}
        PROTOC_OUT_DIR "${PROTO_BINARY_DIR}")
set_source_files_properties(${PROTO_GENERATED_FILES} PROPERTIES SKIP_UNITY_BUILD_INCLUSION on)

message(STATUS "Binary dir: ${PROTO_BINARY_DIR}")
message(STATUS "Files: ${PROTO_GENERATED_FILES}")
target_include_directories(${PROTO_LIBRARY_NAME} PUBLIC ${PROTO_BINARY_DIR})
target_link_libraries(${PROJECT_NAME} PUBLIC ${PROTO_LIBRARY_NAME})
