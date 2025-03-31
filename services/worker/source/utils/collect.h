//
// MIT License
//
// Copyright (c) 2024 Elias Engelbert Plank
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

#ifndef UTILS_COLLECT_H
#define UTILS_COLLECT_H

#include <ranges>

namespace utils {

/// Helper struct that acts as a tag to find the correct operator | overload
namespace detail {
template<typename Container>
struct CollectHelper { };

/// This operator | overload actually collects the range into the container
/// @tparam Container the container type
/// @tparam Range the range type
/// @param r The range
template<typename Container, std::ranges::range Range>
    requires std::convertible_to<std::ranges::range_value_t<Range>, typename Container::value_type>
auto operator|(Range &&r, CollectHelper<Container>) {
    return Container{ r.begin(), r.end() };
}

}// namespace detail

/// Collects a range to the specified container
/// @tparam Container the container type
/// @returns The container filled with the ranges elements
template<std::ranges::range Container>
    requires(not std::ranges::view<Container>)
auto collect() {
    return detail::CollectHelper<Container>{};
}

}// namespace utils

#endif// UTILS_COLLECT_H