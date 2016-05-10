package scala.pickling.`object`.graph.threadsafety.json

import org.scalatest.FunSuite
import scala.pickling._, scala.pickling.Defaults._, json._

class C(val name: String, val desc: String, var c: C, val arr: Array[Int])

class ThreadSafetyTest extends FunSuite {
  val c1 = new C("c1", "desc", null, Array(1))
  val c2 = new C("c2", "desc", c1, Array(1))
  val c3 = new C("c3", "desc", c2, Array(1))
  c1.c = c3

  test("object-graph-threadsafety") {
    val r = new Runnable {
      def run() = {
        for (_ <- 1 to 100) {
          val pickle = c1.pickle
          assert(pickle.toString ===
            """
              |JSONPickle({
              |  "$type": "scala.pickling.object.graph.threadsafety.json.C",
              |  "arr": [
              |    1
              |  ],
              |  "c": {
              |    "$type": "scala.pickling.object.graph.threadsafety.json.C",
              |    "arr": [
              |      1
              |    ],
              |    "c": {
              |      "$type": "scala.pickling.object.graph.threadsafety.json.C",
              |      "arr": [
              |        1
              |      ],
              |      "c": { "$ref": 0 },
              |      "desc": "desc",
              |      "name": "c2"
              |    },
              |    "desc": "desc",
              |    "name": "c3"
              |  },
              |  "desc": "desc",
              |  "name": "c1"
              |})
            """.trim.stripMargin)

          val c11 = pickle.unpickle[C]
          val c13 = c11.c
          val c12 = c13.c
          assert(c11.name === "c1")
          assert(c11.desc === "desc")
          assert(c11.arr.toList === List(1))
          assert(c12.name === "c2")
          assert(c12.desc === "desc")
          assert(c12.arr.toList === List(1))
          assert(c13.name === "c3")
          assert(c13.desc === "desc")
          assert(c13.arr.toList === List(1))
          assert(c12.c === c11)
        }
      }
    }

    val t1 = new Thread(r)
    val t2 = new Thread(r)
    t1.start
    t2.start
    t1.join
    t2.join
  }
}