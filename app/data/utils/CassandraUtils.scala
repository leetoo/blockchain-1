package data.utils

import com.datastax.driver.core.Row
import com.google.common.util.concurrent.{FutureCallback, ListenableFuture, Futures => GFutures}

import scala.concurrent.{Future, Promise}

object CommonConverters {

  object Futures {

      implicit def ResultSetFutureToScalaFuture[A](listenableFuture: ListenableFuture[A]): Future[A] = {
      val promise = Promise[A]()
      GFutures.addCallback(listenableFuture, new FutureCallback[A] {
        def onFailure (t: Throwable): Unit = promise failure t
        def onSuccess (result: A): Unit = promise success result
      })
      promise.future
    }
  }

  implicit class RowExtension(val row: Row) {
    import scala.collection.JavaConverters._

    private def toOption[A](column: String, selector: (Row, String) => A) = {
      if (row.getColumnDefinitions.contains(column)) {
        if (row.isNull(column)) None else Some(selector(row, column))
      }
      else None
    }

    def getOptionUUID(column: String) = toOption(column, _.getUUID(_))
  }

}