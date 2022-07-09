package com.playground

import com.playground.stock.errors.AppError

package object errors {
  type ErrorOr[+A] = Either[AppError, A]
}
