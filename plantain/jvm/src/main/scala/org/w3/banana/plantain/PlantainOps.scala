package org.w3.banana.plantain

import org.w3.banana._
import akka.http.model.Uri
import java.util.UUID
import org.w3.banana.isomorphism._

object PlantainOps extends RDFOps[Plantain] with PlantainMGraphOps with PlantainURIOps {

  // graph

  final val emptyGraph: Plantain#Graph = model.Graph(Map.empty, 0)

  final def makeGraph(triples: Iterable[Plantain#Triple]): Plantain#Graph =
    triples.foldLeft(emptyGraph) { case (g, (s, p, o)) => g + (s, p, o) }

  final def getTriples(graph: Plantain#Graph): Iterable[Plantain#Triple] = graph.triples

  def graphSize(graph: Plantain#Graph): Int = graph.size

  // triple

  final def makeTriple(s: Plantain#Node, p: Plantain#URI, o: Plantain#Node): Plantain#Triple =
    (s, p, o)

  final def fromTriple(t: Plantain#Triple): (Plantain#Node, Plantain#URI, Plantain#Node) = t

  // node

  final def foldNode[T](
    node: Plantain#Node)(
    funURI: Plantain#URI => T,
    funBNode: Plantain#BNode => T,
    funLiteral: Plantain#Literal => T
  ): T = node match {
    case uri: Uri               => funURI(uri)
    case bnode: model.BNode     => funBNode(bnode)
    case literal: model.Literal => funLiteral(literal)
  }

  // URI

  final def fromUri(uri: Plantain#URI): String = uri.toString

  final def makeUri(s: String): Plantain#URI = Uri(s)

  // bnode

  final def makeBNode(): Plantain#BNode = model.BNode(java.util.UUID.randomUUID().toString)

  final def makeBNodeLabel(label: String): Plantain#BNode = model.BNode(label)

  final def fromBNode(bnode: Plantain#BNode): String = bnode.label

  // literal

  final val __rdfLangString = makeUri("http://www.w3.org/1999/02/22-rdf-syntax-ns#langString")

  final def makeLiteral(lexicalForm: String, datatype: Plantain#URI): Plantain#Literal =
    model.Literal(lexicalForm, datatype, null)

  final def makeLangTaggedLiteral(lexicalForm: String, lang: Plantain#Lang): Plantain#Literal =
    model.Literal(lexicalForm, __rdfLangString, lang)

  final def fromLiteral(literal: Plantain#Literal): (String, Plantain#URI, Option[Plantain#Lang]) =
    (literal.lexicalForm, literal.datatype, Option(literal.langOpt))

  // lang

  final def makeLang(langString: String): Plantain#Lang = langString

  final def fromLang(lang: Plantain#Lang): String = lang

  // graph traversal

  final val ANY: Plantain#NodeAny = null

  implicit def toConcreteNodeMatch(node: Plantain#Node): Plantain#NodeMatch = node

  final def foldNodeMatch[T](
    nodeMatch: Plantain#NodeMatch)(
    funANY: => T,
      funConcrete: Plantain#Node => T
  ): T = nodeMatch match {
    case null => funANY
    case node => funConcrete(node)
  }

  final def find(
    graph: Plantain#Graph,
    subject: Plantain#NodeMatch,
    predicate: Plantain#NodeMatch,
    objectt: Plantain#NodeMatch
  ): Iterator[Plantain#Triple] = predicate match {
    case p: Uri => graph.find(Option(subject), Some(p), Option(objectt)).iterator
    case null   => graph.find(Option(subject), None, Option(objectt)).iterator
    case p      => sys.error(s"[find] invalid value in predicate position: $p")
  }

  // graph union

  final def union(graphs: Seq[Plantain#Graph]): Plantain#Graph = {
    var mgraph = makeEmptyMGraph()
    graphs.foreach(graph => addTriples(mgraph, graph.triples))
    mgraph.graph
  }

  final def diff(g1: Plantain#Graph, g2: Plantain#Graph): Plantain#Graph = {
    val mgraph = makeMGraph(g1)
    try { removeTriples(mgraph, g2.triples) } catch { case nsee: NoSuchElementException => () }
    mgraph.graph
  }

  // graph isomorphism

  final def isomorphism(left: Plantain#Graph, right: Plantain#Graph): Boolean = {
    import Util.toSesameGraph
    org.openrdf.model.util.ModelUtil.equals(toSesameGraph(left), toSesameGraph(right))
  }

}
