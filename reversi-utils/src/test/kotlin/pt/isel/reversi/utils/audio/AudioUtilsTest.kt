package pt.isel.reversi.utils.audio

import kotlin.test.*
import javax.sound.sampled.*
import pt.isel.reversi.utils.audio.controls.FloatControlWrapper
import pt.isel.reversi.utils.audio.controls.BooleanControlWrapper

// Minimal test subclasses for FloatControl and BooleanControl to allow construction in tests
// Try to match available FloatControl constructor overloads commonly present in JDKs.
class TestFloatControl(type: Type, minimum: Float, maximum: Float, default: Float) : FloatControl(
    type,
    minimum,
    maximum,
    0f,
    0,
    default,
    ""
)

class TestBooleanControl(type: Type, default: Boolean) : BooleanControl(type, default)

/**
 * A minimal fake Clip implementation that supports the operations used by the utility classes under test.
 * Only implements the subset of methods required by the tests. Other methods are provided with basic no-op
 * implementations or reasonable defaults.
 */
class FakeClip(private val controlsMap: Map<Control.Type, Control> = emptyMap()) : Clip {
    private var _closed: Boolean = false
    override fun close() { _closed = true }

    override fun isOpen(): Boolean = !_closed

    private var _running: Boolean = false
    override fun isRunning(): Boolean = _running

    override fun start() { _running = true }
    override fun stop() { _running = false }

    private var _loopPoints: Pair<Int, Int>? = null
    private var _loopCountSet: Int? = null
    override fun loop(count: Int) { _loopCountSet = count; _running = true }
    override fun setLoopPoints(start: Int, end: Int) { _loopPoints = Pair(start, end) }

    override fun addLineListener(listener: LineListener?) {}
    override fun removeLineListener(listener: LineListener?) {}

    // Controls
    override fun getControl(control: Control.Type?): Control {
        return controlsMap[control] ?: throw IllegalArgumentException("Control not available: $control")
    }

    override fun isControlSupported(control: Control.Type?): Boolean = controlsMap.containsKey(control)

    // Frame related - provide methods required by Clip/DataLine
    private var _framePosition: Int = 0
    override fun setFramePosition(position: Int) { _framePosition = position }
    override fun getFramePosition(): Int = _framePosition
    override fun getFrameLength(): Int = 1024

    // additional methods
    override fun getLongFramePosition(): Long = _framePosition.toLong()
    override fun isActive(): Boolean = _running
    override fun available(): Int = 0

    // Audio format and buffer methods (no-op / defaults)
    override fun getFormat(): AudioFormat? = null
    override fun getBufferSize(): Int = 0
    override fun getLevel(): Float = 0f

    // Unused methods from DataLine / Line
    // Provide multiple overloads matching DataLine/Clip API
    override fun open() {}
    override fun open(format: AudioFormat?, audioData: ByteArray?, offset: Int, bufferSize: Int) {}
    override fun open(stream: AudioInputStream?) {}

    override fun drain() {}
    override fun flush() {}
    override fun getLineInfo(): Line.Info = DataLine.Info(Clip::class.java, null)

    override fun getControls(): Array<Control> = controlsMap.values.toTypedArray()
    override fun getMicrosecondPosition(): Long = 0L
    override fun setMicrosecondPosition(value: Long) {}
    override fun getMicrosecondLength(): Long = 0L
    // removed unsupported overloads

    // Expose inspection helpers for tests
    val loopPoints: Pair<Int, Int>?
        get() = _loopPoints
    val loopCountSet: Int?
        get() = _loopCountSet
    val closed: Boolean
        get() = _closed
}


class AudioUtilsTests {

    @Test
    fun audioModifierCopyHelpersProduceExpectedValues() {
        val m = AudioModifier()
        assertFalse(m.closeOnFinish)
        assertTrue(m.gotoStartOnStop)

        val m1 = m.setToCloseOnFinish()
        assertTrue(m1.closeOnFinish)

        val m2 = m.setToNotGoToStartOnStop()
        assertFalse(m2.gotoStartOnStop)

        val m3 = m.setStartPosition(10)
        assertEquals(10, m3.startPosition)

        val m4 = m.setToLoop(5, 20)
        assertTrue(m4.loop)
        assertEquals(5, m4.loopStartPosition)
        assertEquals(20, m4.loopEndPosition)

        val m5 = m.setToLoopInfinitely()
        assertTrue(m5.loop)
        assertEquals(0, m5.loopStartPosition)
        assertEquals(-1, m5.loopEndPosition)
    }

    @Test
    fun floatControlWrapperReadsAndUpdatesValuesWithClampingAndReset() {
        val master = TestFloatControl(FloatControl.Type.MASTER_GAIN, -10f, 10f, 0f)
        val pan = TestFloatControl(FloatControl.Type.PAN, -1f, 1f, 0f)
        val balance = TestFloatControl(FloatControl.Type.BALANCE, -1f, 1f, 0f)

        val controls = mapOf<Control.Type, Control>(
            FloatControl.Type.MASTER_GAIN to master,
            FloatControl.Type.PAN to pan,
            FloatControl.Type.BALANCE to balance
        )

        val clip = FakeClip(controls)

        val masterWrapper = FloatControlWrapper.MasterVolumeControl(clip)
        val panWrapper = FloatControlWrapper.PanControl(clip)
        val balWrapper = FloatControlWrapper.BalanceControl(clip)

        // defaults
        assertEquals(0f, masterWrapper.getValue())
        assertEquals(0f, panWrapper.getValue())
        assertEquals(0f, balWrapper.getValue())

        // update with clamping
        masterWrapper.updateValue(20f)
        assertEquals(10f, masterWrapper.getValue())

        panWrapper.updateValue(-2f)
        assertEquals(-1f, panWrapper.getValue())

        // add value
        balWrapper.addValue(0.5f)
        assertEquals(0.5f, balWrapper.getValue())

        // reset
        masterWrapper.resetValue()
        assertEquals(0f, masterWrapper.getValue())
    }

    @Test
    fun booleanControlWrapperReadsUpdatesAndResets() {
        val mute = TestBooleanControl(BooleanControl.Type.MUTE, false)
        val controls = mapOf<Control.Type, Control>(BooleanControl.Type.MUTE to mute)
        val clip = FakeClip(controls)

        val muteWrapper = BooleanControlWrapper.MuteControl(clip)
        assertFalse(muteWrapper.getValue())

        muteWrapper.updateValue(true)
        assertTrue(muteWrapper.getValue())

        muteWrapper.resetValue()
        assertFalse(muteWrapper.getValue())
    }

    @Test
    fun audioWrapperPlayPauseStopBehaviorWithModifier() {
        val master = TestFloatControl(FloatControl.Type.MASTER_GAIN, -10f, 10f, 0f)
        val bal = TestFloatControl(FloatControl.Type.BALANCE, -1f, 1f, 0f)
        val mute = TestBooleanControl(BooleanControl.Type.MUTE, false)
        val controls = mapOf<Control.Type, Control>(
            FloatControl.Type.MASTER_GAIN to master,
            FloatControl.Type.BALANCE to bal,
            BooleanControl.Type.MUTE to mute
        )

        val fakeClip = FakeClip(controls)
        fakeClip.setFramePosition(5)

        val modifier = AudioModifier(startPosition = 5, loop = true, loopStartPosition = 2, loopEndPosition = 50, gotoStartOnStop = true, closeOnFinish = false)
        val audio = AudioWrapper("t1", fakeClip, modifier)

        // initial framePosition set in init
        assertEquals(5, fakeClip.getFramePosition())

        // play should set loop points and call loop with LOOP_CONTINUOUSLY
        audio.play()
        assertTrue(fakeClip.isRunning())
        assertEquals(Pair(2, 50), fakeClip.loopPoints)
        assertEquals(Clip.LOOP_CONTINUOUSLY, fakeClip.loopCountSet)

        // pause should stop running
        audio.pause()
        assertFalse(fakeClip.isRunning())

        // stop should reset framePosition to 0 (gotoStartOnStop true)
        audio.stop()
        assertEquals(0, fakeClip.getFramePosition())
        assertFalse(fakeClip.closed)

        // stop with closeOnFinish true
        val modifier2 = modifier.setToCloseOnFinish()
        val fakeClip2 = FakeClip(controls)
        val audio2 = AudioWrapper("t2", fakeClip2, modifier2)
        audio2.stop()
        assertTrue(fakeClip2.closed)
    }

    @Test
    fun audioPoolOperationsAndVolumeBalanceSyncBehavior() {
        val m1 = TestFloatControl(FloatControl.Type.MASTER_GAIN, -10f, 10f, 0f)
        val b1 = TestFloatControl(FloatControl.Type.BALANCE, -1f, 1f, 0.2f)
        val m2 = TestFloatControl(FloatControl.Type.MASTER_GAIN, -10f, 10f, 0f)
        val b2 = TestFloatControl(FloatControl.Type.BALANCE, -1f, 1f, -0.3f)
        val mute = TestBooleanControl(BooleanControl.Type.MUTE, false)

        val c1 = mapOf<Control.Type, Control>(FloatControl.Type.MASTER_GAIN to m1, FloatControl.Type.BALANCE to b1, BooleanControl.Type.MUTE to mute)
        val c2 = mapOf<Control.Type, Control>(FloatControl.Type.MASTER_GAIN to m2, FloatControl.Type.BALANCE to b2, BooleanControl.Type.MUTE to mute)

        val clip1 = FakeClip(c1)
        val clip2 = FakeClip(c2)

        val a1 = AudioWrapper("a1", clip1)
        val a2 = AudioWrapper("a2", clip2)

        val pool = AudioPool(listOf(a1, a2))
        // play/stop/pause
        pool.play("a1")
        assertTrue(clip1.isRunning())
        pool.pause("a1")
        assertFalse(clip1.isRunning())
        pool.playAll()
        assertTrue(clip1.isRunning() && clip2.isRunning())
        pool.stopAll()
        assertFalse(clip1.isRunning() || clip2.isRunning())

        // master volume change & set
        pool.setMasterVolume(5f)
        assertEquals(5f, pool.getMasterVolume())
        pool.changeMasterVolume(-2f)
        assertEquals(3f, pool.getMasterVolume())

        // balance get fixes inconsistent balances
        val firstBalance = pool.getBalance()
        assertNotNull(firstBalance)
        assertEquals(firstBalance, (b1).value)
        assertEquals(firstBalance, (b2).value)

        // mute
        pool.mute(true)
        assertTrue((mute).value)
        pool.mute(false)
        assertFalse((mute).value)

        // isPoolStopped
        assertTrue(pool.isPoolStopped())
    }
}
