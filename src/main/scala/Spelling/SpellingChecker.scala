package Spelling

import DB.WordDB
import zio.{Task, ZLayer, ZIO}

class SpellingChecker(db: WordDB) {
  val NIterations: Int = 2
  val MinProb: Int = 500

  /*
   * Method suggest takes a word and returns the closest word in the database WordDB db.
   * It generates a list of similar words and checks which ones are in the WordDB.
   * NIterations is the maximum number of iterations in which similar words to item will be produced
   * if the probability of the words given by db is less than MinProb.
   */
  def suggest(item: String): Task[String] =
    SpellingChecker.join(SpellingChecker
      .parse(item)
      .map(word => suggestWord(List(word), NIterations)))

  /*
   * It returns the most similar word to the words in items allowing for n iterations of producing similar words.
   */
  def suggestWord(items: List[String], n: Int): Task[String] =
    if (n < 1) ZIO succeed items.head
    else for {
      near <- ZIO succeed items.flatMap(SpellingChecker.getNear(_))
      coincidences <- db.coincidences(near)
      suggested <- coincidences.headOption match {
        case None => suggestWord(near, n - 1)
        case Some(word) => ZIO.ifZIO(db.prob(word).map(_ > MinProb))(
          onTrue = ZIO succeed word,
          onFalse = suggestWord(near, n - 1)
        )
      }
    } yield suggested



}


object SpellingChecker {

  val layer: ZLayer[Any, Throwable, SpellingChecker] = ZLayer {
    for {
      db <- ZIO.attempt(new WordDB)
    } yield new SpellingChecker(db)
  }

  /*
   * Method parse divide a string by the instances of " " and return the parts in a list.
   */
  def parse(sentence: String): List[String] = getWord(sentence) match {
    case ("", "") => List()
    case (fst, "") => fst :: List()
    case ("", snd) => parse(snd)
    case (fst, snd) => fst :: parse(snd)
  }

  // It returns a pair (a, b) where a is the first word in sentence and b is the rest of the sentence.
  def getWord(sentence: String): (String, String) = sentence.headOption match {
    case None => ("", "")
    case Some(' ') => ("", sentence.tail)
    case Some(char) => {
      val asd = getWord(sentence.tail)
      (char + asd._1, asd._2)
    }
  }

  /*
   * It provides a list with all possible splittings of item.
   */
  def splitEverywhere(item: String): List[(String, String)] = {
    for (i <- 0 to item.length) yield item.splitAt(i)
  }.toList

  /*
   * It provides similar words to item: inserting and deleting letters.
   */
  def getNear(item: String): List[String] = {
    val letters: List[Char] = ('a' to 'z').toList
    val splitted: List[(String, String)] = splitEverywhere(item)
    val deleting: List[String] = splitted.map(x => x._1 + x._2.tail)
    val inserting: List[String] = letters.flatMap(letter => splitted.map(x => x._1 + letter + x._2)
    )
    deleting ++ inserting
  }

  // It takes a list of strings and concatenates it with " ".
  def join(suggested: List[Task[String]]): Task[String] =
    if (suggested.nonEmpty) {
      suggested.tail.foldLeft(suggested.head) {
        case (stringZIO, rest) => {
          for {
            string <- stringZIO
            nextWord <- rest.catchAll(_ => ZIO succeed "")
          } yield string + " " + nextWord
        }
      }
    } else ZIO succeed ""



}
