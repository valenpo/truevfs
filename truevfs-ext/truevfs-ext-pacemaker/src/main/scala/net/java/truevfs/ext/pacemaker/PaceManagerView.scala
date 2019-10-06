/*
 * Copyright (C) 2005-2015 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.truevfs.ext.pacemaker

import javax.management.{MBeanAttributeInfo, MBeanInfo}
import net.java.truevfs.comp.jmx.JmxManagerView

/** A view for a [[PaceManager]].
  * This class is thread-safe.
  *
  * @author Christian Schlichtherle
  */
private final class PaceManagerView(manager: PaceManager)
  extends JmxManagerView[PaceManager](classOf[PaceManagerMXBean], manager)
    with PaceManagerMXBean {

  override protected def getDescription(info: MBeanInfo): String = "A pace maker for the file system manager."

  override protected def getDescription(info: MBeanAttributeInfo): String = {
    info.getName match {
      case "MaximumFileSystemsMounted" => "The maximum number of mounted file systems."
      case _ => super.getDescription(info)
    }
  }

  override def getMaximumFileSystemsMounted: Int = manager.maximumSize

  override def setMaximumFileSystemsMounted(maximumSize: Int): Unit = manager.maximumSize = maximumSize
}
