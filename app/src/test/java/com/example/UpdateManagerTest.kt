package com.example

import com.example.util.UpdateManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateManagerTest {

    @Test
    fun testIsNewerVersion_remoteIsNewer() {
        assertTrue(UpdateManager.isNewerVersion("1.0.0", "1.0.1"))
        assertTrue(UpdateManager.isNewerVersion("1.0.0", "1.1.0"))
        assertTrue(UpdateManager.isNewerVersion("1.0.0", "2.0.0"))
        assertTrue(UpdateManager.isNewerVersion("1.2.0", "v1.3.0"))
        assertTrue(UpdateManager.isNewerVersion("1.4.1", "1.4.2"))
        assertTrue(UpdateManager.isNewerVersion("1.4.1", "v1.5.0"))
        assertTrue(UpdateManager.isNewerVersion("1.2.0", "v1.2.0.1"))
    }

    @Test
    fun testIsNewerVersion_remoteIsSameOrOlder() {
        assertFalse(UpdateManager.isNewerVersion("1.0.0", "1.0.0"))
        assertFalse(UpdateManager.isNewerVersion("1.4.1", "v1.4.1"))
        assertFalse(UpdateManager.isNewerVersion("1.4.1", "1.4.1"))
        assertFalse(UpdateManager.isNewerVersion("2.0.0", "1.9.9"))
        assertFalse(UpdateManager.isNewerVersion("1.5.0", "v1.4.9"))
    }

    @Test
    fun testIsNewerVersion_invalidOrBlankFormats() {
        assertFalse(UpdateManager.isNewerVersion("1.0.0", ""))
        assertFalse(UpdateManager.isNewerVersion("", "1.0.0"))
        assertFalse(UpdateManager.isNewerVersion("invalid", "invalid"))
    }
}
