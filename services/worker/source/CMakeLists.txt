#
# MIT License
#
# Copyright (c) 2025 Elias Engelbert Plank
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.

# Project source files
file(GLOB PROJECT_SOURCES
        "${CMAKE_CURRENT_SOURCE_DIR}/*.h"
        "${CMAKE_CURRENT_SOURCE_DIR}/*.cpp"
        "${CMAKE_CURRENT_SOURCE_DIR}/utils/*.h"
        "${CMAKE_CURRENT_SOURCE_DIR}/utils/*.cpp"
)

# Executable definition with the project sources
add_executable("${PROJECT_NAME}"
        "${PROJECT_SOURCES}"
)

target_link_libraries("${PROJECT_NAME}" PRIVATE whisper spdlog::spdlog nlohmann_json tl::expected CURL::libcurl)

# Include directories for ffmpeg libraries
target_include_directories("${PROJECT_NAME}" PRIVATE
        "${AVFORMAT_INCLUDE_DIRS}"
        "${AVCODEC_INCLUDE_DIRS}"
        "${AVUTIL_INCLUDE_DIRS}"
        "${SWSCALE_INCLUDE_DIRS}"
        "${SWRESAMPLE_INCLUDE_DIRS}"
)

# Link ffmpeg libraries
target_link_libraries("${PROJECT_NAME}" PRIVATE
        "${AVFORMAT_LIBRARIES}"
        "${AVCODEC_LIBRARIES}"
        "${AVUTIL_LIBRARIES}"
        "${SWSCALE_LIBRARIES}"
        "${SWRESAMPLE_LIBRARIES}"
)
