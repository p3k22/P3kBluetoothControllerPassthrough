package com.p3kIndustries.p3kps4bt2pc.events

import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import com.p3kIndustries.p3kps4bt2pc.models.Input
import com.p3kIndustries.p3kps4bt2pc.services.Logger
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class Device(private val logger: Logger?) {

    private val mutex = Mutex()
    private val input = Input()

    // Thread-safe access to controller input data
    suspend fun getControllerInput(): Input
    {
        return mutex.withLock {
            val copy = input.copy(keyEvents = input.keyEvents.toMutableList())
            input.keyEvents.clear()
            copy
        }
    }

    suspend fun handleMotionEvent(event: MotionEvent?) {
        event?.let {
            val inputDevice = event.device
            val deviceName = inputDevice.name
            //logger.addLog("Input from device: $deviceName (ID: $deviceId)")
            if (event.source and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK) {
                mutex.withLock {
                    input.leftStickX = event.getAxisValue(MotionEvent.AXIS_X)
                    input.leftStickY = event.getAxisValue(MotionEvent.AXIS_Y)
                    input.rightStickX = event.getAxisValue(MotionEvent.AXIS_Z)
                    input.rightStickY = event.getAxisValue(MotionEvent.AXIS_RZ)
                    input.l2 = event.getAxisValue(MotionEvent.AXIS_LTRIGGER)
                    input.r2 = event.getAxisValue(MotionEvent.AXIS_RTRIGGER)
                    input.dpadX = event.getAxisValue(MotionEvent.AXIS_HAT_X)
                    input.dpadY = event.getAxisValue(MotionEvent.AXIS_HAT_Y)
                }
            }
        }
    }

    suspend fun handleKeyEvent(event: KeyEvent?, isKeyDown: Boolean) {
        event?.let {
            val action = if (isKeyDown) "pressed" else "released"
            val keyEventDescription = when (event.keyCode) {
                KeyEvent.KEYCODE_BUTTON_A -> "Cross button $action"
                KeyEvent.KEYCODE_BUTTON_B -> "Circle button $action"
                KeyEvent.KEYCODE_BUTTON_X -> "Square button $action"
                KeyEvent.KEYCODE_BUTTON_Y -> "Triangle button $action"
                KeyEvent.KEYCODE_BUTTON_L1 -> "L1 button $action"
                KeyEvent.KEYCODE_BUTTON_R1 -> "R1 button $action"
                KeyEvent.KEYCODE_BUTTON_SELECT -> "Share button $action"
                KeyEvent.KEYCODE_BUTTON_START -> "Options button $action"
                KeyEvent.KEYCODE_BUTTON_THUMBL -> "Left stick button $action"
                KeyEvent.KEYCODE_BUTTON_THUMBR -> "Right stick button $action"
                KeyEvent.KEYCODE_DPAD_UP -> "D-pad Up $action"
                KeyEvent.KEYCODE_DPAD_DOWN -> "D-pad Down $action"
                KeyEvent.KEYCODE_DPAD_LEFT -> "D-pad Left $action"
                KeyEvent.KEYCODE_DPAD_RIGHT -> "D-pad Right $action"
                KeyEvent.KEYCODE_BUTTON_MODE -> "PS button $action"
                else -> null
            }

            keyEventDescription?.let { description ->
                mutex.withLock {
                    if (isKeyDown) {
                        if (!input.keyEvents.contains(description)) {
                            input.keyEvents.add(description)
                        }else{

                        }
                    } else {
                        input.keyEvents.remove(description)
                    }
                }
            }
        }
    }
}