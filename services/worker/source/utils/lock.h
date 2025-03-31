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
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

#ifndef UTILS_LOCK_H
#define UTILS_LOCK_H

#include <memory>
#include <mutex>
#include <shared_mutex>

namespace utils {

/// Implementation specific details
namespace details {

/// Lock type for read-write access to the shared mutex
using UniqueLockType = std::unique_lock<std::shared_mutex>;

/// Lock type for read access to the shared mutex
using SharedLockType = std::shared_lock<std::shared_mutex>;

/// Concept that states that the provided type must be
/// of the same type as the {@link UniqueLockType}
template<typename Type>
concept UniqueLock = std::same_as<Type, UniqueLockType>;

/// Concept that states that the provided type must be
/// of the same type as the {@link SharedLockType}
template<typename Type>
concept SharedLock = std::same_as<Type, SharedLockType>;

}// namespace details

/// RAII wrapper for accessing variables owned by the shared mutex guard
/// @tparam Type The type of the variable that is owned by the shared mutex
/// guard
/// @tparam LockType The type of the lock that controls access permissions
template<typename Type, typename LockType>
    requires(details::UniqueLock<LockType> or details::SharedLock<LockType>)
struct LockGuard {
private:
    Type *value;
    LockType lock;

    /// Constructs a LockGuard with the value that is owned by the mutex guard
    /// @param value The value that is owned by the mutex guard
    /// @param mutex The actual shared mutex of the mutex guard
    LockGuard(Type *value, std::shared_mutex &mutex) : value(value), lock(mutex) { }

    /// Declare the shared mutex guard as a friend in order to access the private
    /// ctor
    template<typename U>
    friend struct ReadWriteLock;

public:
    /// Read-write access to the value
    /// @return A pointer to the value
    [[nodiscard]] auto operator->()
        requires(not std::is_const_v<Type> and details::UniqueLock<LockType>)
    {
        return value;
    }

    /// Read access to the value
    /// @return A const pointer to the value
    [[nodiscard]] Type const *operator->() const {
        return value;
    }

    /// Read-write access to the value
    /// @return A reference to the value
    [[nodiscard]] auto &operator*()
        requires(not std::is_const_v<Type> and details::UniqueLock<LockType>)
    {
        return *value;
    }

    /// Read access to the value
    /// @return A const reference to the value
    [[nodiscard]] Type const &operator*() const {
        return *value;
    }
};

/// Data structure for controlling access to a variable via a shared mutex.
/// Access is only granted via the LockGuard type that internally controls
/// access permissions, depending on the lock that was placed upon the shared
/// mutex.
/// @tparam Type The type of the variable that is owned by the shared mutex
/// guard
template<typename Type>
struct ReadWriteLock {
private:
    std::unique_ptr<Type> value;
    mutable std::shared_mutex mutex;

    template<typename LockType>
    using LockGuardType = LockGuard<Type, LockType>;

    using UniqueLockType = std::unique_lock<std::shared_mutex>;
    using SharedLockType = std::shared_lock<std::shared_mutex>;

public:
    /// Creates a shared mutex guard wrapper around the owned variable
    /// @param args Constructor arguments that are passed to the owned variable
    explicit ReadWriteLock(auto &&...args) : value(std::make_unique<Type>(std::forward<decltype(args)>(args)...)) { }
    ~ReadWriteLock() noexcept = default;

    /// Exclusively locks the mutex for read-write access
    /// @return A LockGuard wrapper around the owned variable
    [[nodiscard]] auto lock() & {
        return LockGuardType<details::UniqueLockType>(value.get(), mutex);
    }

    /// Shared locks the mutex for read access
    /// @return A LockGuard wrapper around the owned variable
    [[nodiscard]] LockGuardType<SharedLockType> shared_lock() const & {
        return LockGuardType<details::SharedLockType>(value.get(), mutex);
    }
};

}// namespace utils

#endif// UTILS_LOCK_H
