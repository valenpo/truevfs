/*
 * Copyright (C) 2005-2012 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.truevfs.kernel.util

import collection._
import FileSystem._

/**
 * A (virtual) file system is a mutable map from decomposable keys, named
 * <em>paths</em>, to arbitrary values, named <em>entries</em>.
 * The standard use case for this class is for mapping path name strings which
 * can get decomposed into segments by splitting them with a separator
 * character such as {@code '/'}.
 * However, this class works with any generic decomposable key type for which
 * a {@link Composition} and a {@link DirectoryFactory} exist.
 * <p>
 * Using this class helps to save some heap space if the paths address deeply
 * nested directory trees where many path segments can get shared between
 * mapped entries &mdash; provided that no other references to the paths are
 * held!
 * <p>
 * This class supports both {@code null} paths and entries.
 * <p>
 * This class is <em>not</em> thread-safe!
 * 
 * @param  <K> the type of the paths (keys) in this (virtual) file system (map).
 * @param  <V> the type of the entries (values) in this (virtual) file system
 *         (map).
 * @author Christian Schlichtherle
 */
final class FileSystem[K >: Null, V](
  implicit val composition: Composition[K],
  val directoryFactory: DirectoryFactory[K]
) extends mutable.Map[K, V] with mutable.MapLike[K, V, FileSystem[K, V]] {

  private[this] var _root: Node[K, V] = _
  private var _size: Int = _

  reset()

  implicit private def self = this

  private def reset() { _root = new Root; _size = 0}

  override def size = _size

  override def clear() = reset()

  override def empty = new FileSystem[K, V]

  override def iterator = _root recursiveEntriesIterator (None)

  override def get(path: K) = node(path) flatMap (_ entry)

  def node(path: K): Option[Node[K, V]] = node(Option(path))

  private def node(path: Option[K]): Option[Node[K, V]] = {
    path match {
      case Some(path) =>
        path match {
          case composition(parent, segment) =>
            node(parent) flatMap (_ get(segment))
        }
      case None =>
        Some(_root)
    }
  }

  def list(path: K): Option[Iterable[(K, V)]] = list(Option(path))

  private def list(path: Option[K]) = node(path) map (_ list path)

  override def +=(kv: (K, V)) = { link(kv._1, kv._2); this }

  def link(path: K, entry: V): Node[K, V] = link(Option(path), Some(entry))

  private def link(path: Option[K], entry: Option[V]): Node[K, V] = {
    path match {
      case Some(path) =>
        path match {
          case composition(parent, segment) =>
            link(parent, None) link (segment, entry)
        }
      case None =>
        if (entry isDefined) _root entry = entry
        _root
    }
  }

  override def -=(path: K) = { unlink(path); this }

  def unlink(path: K): Unit = unlink(Option(path))

  private def unlink(path: Option[K]) {
    path match {
      case Some(path) =>
        path match {
          case composition(parent, segment) =>
            node(parent) foreach { node =>
              node unlink segment
              if (node isEmpty) unlink(parent)
            }
        }
      case None =>
        _root entry = None
    }
  }

  override def stringPrefix = "FileSystem"
} // FileSystem

object FileSystem {

  def apply[K >: Null, V](
    composition: Composition[K],
    directoryFactory: DirectoryFactory[K]
  ) = new FileSystem[K, V]()(composition, directoryFactory)

  def apply[V](
    separator: Char,
    directoryFactory: DirectoryFactory[String] = new SortedDirectoryFactory
  ) = apply[String, V](new StringComposition(separator), directoryFactory)

  sealed class Node[K >: Null, V] protected (
    private[this] val parent: Option[(Node[K, V], K)],
    private[this] var _entry: Option[V]
  ) (implicit fs: FileSystem[K, V]) {

    private[this] val _members = fs.directoryFactory.create[Node[K, V]]

    if (_entry isDefined) fs._size += 1

    def address: (FileSystem[K, V], Option[K]) = {
      val (node, segment) = parent get
      val (fs, path) = node.address
      fs -> Some(fs composition (path, segment))
    }

    final def path = address _2

    final def entry = _entry

    private[FileSystem] final def entry_=(entry: Option[V])
    (implicit fs: FileSystem[K, V]) {
      // HC SVNT DRACONES!
      if (_entry isDefined) {
        if (entry isEmpty)
          fs._size -= 1
      } else {
        if (entry isDefined)
          fs._size += 1
      }
      _entry = entry
    }

    private[FileSystem] final def isEmpty = _entry.isEmpty && 0 == _members.size

    private[FileSystem] final def get(segment: K) = _members get segment

    private[FileSystem] final def link(segment: K, entry: Option[V])
    (implicit fs: FileSystem[K, V]) = {
      _members get segment match {
        case Some(node) =>
          if (entry isDefined) node entry = entry
          node
        case None =>
          val node = new Node[K, V](Some(this, segment), entry)
          _members += segment -> node
          node
      }
    }

    private[FileSystem] final def unlink(segment: K)
    (implicit fs: FileSystem[K, V]) {
      _members get segment map { node =>
        node entry = None
        if (node isEmpty) _members -= segment
      }
    }

    private[FileSystem] final def recursiveEntriesIterator(path: Option[K])
    (implicit composition: Composition[K]): Iterator[(K, V)] = {
      entry(path).iterator ++ _members.iterator.flatMap {
        case (segment, node) =>          
          node recursiveEntriesIterator Some(composition(path, segment))
      }
    }

    private def entry(path: Option[K]) = _entry map (path.orNull -> _)

    private[FileSystem] def list(path: Option[K])
    (implicit composition: Composition[K]) = {
      _members.toIterable flatMap {
        case (segment, node) => node.entry map (composition(path, segment) -> _)
      }
    }

    final def members = {
      _members.toIterable withFilter {
        case (segment, node) => node.entry isDefined
      }
    }

    final override def toString = "Node(path=" + path + ", entry=" + entry + ")"
  } // Node

  private final class Root[K >: Null, V]
  (implicit fs: FileSystem[K, V])
  extends Node[K, V](None, None) {
    override def address = fs -> None
  } // Root

  trait Composition[K] extends ((Option[K], K) => K) {
    /** The composition method for injection. */
    override def apply(parent: Option[K], segment: K): K

    /**
     * The decomposition method for extraction.
     * Note that the second element of the returned tuple should not share any
     * memory with the given path - otherwise you might not achieve any heap
     * space savings!
     */
    def unapply(path: K): Some[(Option[K], K)]
  } // Composition

  final class StringComposition(separator: Char)
  extends Composition[String] {
    override def apply(parent: Option[String], segment: String) = {
      parent match {
        case Some(parent) => parent + segment
        case None => segment
      }
    }

    override def unapply(path: String) = {
      val i = path.lastIndexOf(separator)
      if (0 <= i) {
        // Don't share sub-strings with the FileSystem!
        Some(Some(path substring (0, i)) -> new String(path substring i))
      } else {
        // There's no point in copying here!
        Some(None, path)
      } 
    }
  } // StringComposition

  trait DirectoryFactory[K] {
    def create[V]: mutable.Map[K, V]
  } // DirectoryFactory

  final class HashedDirectoryFactory[K] extends DirectoryFactory[K] {
    def create[V] = new mutable.HashMap
  } // HashDirectoryFactory

  final class SortedDirectoryFactory[K : Ordering] extends DirectoryFactory[K] {
    def create[V] = new mutable.ImmutableMapAdaptor(immutable.SortedMap.empty)
  } // SortedDirectoryFactory

  final class LinkedDirectoryFactory[K] extends DirectoryFactory[K] {
    def create[V] = new mutable.LinkedHashMap
  }
} // FileSystem
