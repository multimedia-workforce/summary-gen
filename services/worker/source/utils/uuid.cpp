//
// MIT License
//
// Copyright (c) 2025 Elias Engelbert Plank
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

#include "uuid.h"

#include <iomanip>
#include <random>
#include <sstream>

namespace utils {

std::string UUID::generate_v4() {
    thread_local std::random_device rd;
    thread_local std::mt19937 gen(rd());
    std::uniform_int_distribution<uint32_t> dist(0, 0xFFFFFFFF);

    auto to_hex = [](uint32_t const value, int const width) {
        std::ostringstream oss;
        oss << std::hex << std::setw(width) << std::setfill('0') << value;
        return oss.str();
    };

    auto const part1 = dist(gen);
    auto const part2 = dist(gen) & 0xFFFF;
    auto const part3 = (dist(gen) & 0x0FFF) | 0x4000;// version 4
    auto const part4 = (dist(gen) & 0x3FFF) | 0x8000;// variant 1 (10xx)
    auto const part5 = (static_cast<uint64_t>(dist(gen)) << 32) | dist(gen);

    std::ostringstream oss;
    oss << to_hex(part1, 8) << "-" << to_hex(part2, 4) << "-" << to_hex(part3, 4) << "-" << to_hex(part4, 4) << "-"
        << to_hex(static_cast<uint32_t>(part5 >> 32), 8)
        << to_hex(static_cast<uint32_t>(part5 & 0xFFFFFFFF), 8).substr(0, 4);// total 12 hex digits

    return oss.str();
}

}// namespace utils
