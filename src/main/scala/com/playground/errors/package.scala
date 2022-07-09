package com.playground

package object errors {
  type ErrorOr[+A] = Either[AppError, A]
}
