package com.example

class KotlinErrors {
    // This function name is too short, which will trigger the FunctionNameTooShort rule
    fun fn() {
        println("This function name is too short")
    }
}

// This class overrides equals but not hashCode, which will trigger the OverrideBothEqualsAndHashcode rule
class EqualsOnly {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }
    
    // Missing hashCode() override
}