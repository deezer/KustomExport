package deezer.kustom.compiler.js

/**
 * You should create 1 instance of this class by generated class.
 */
class MethodNameDisambiguation {
    private val generatedNames = mutableMapOf<FunctionDescriptor, String>()

    fun getMethodName(origin: FunctionDescriptor): String {
        generatedNames[origin]?.let { return it } // Already in cache

        if (!generatedNames.containsValue(origin.name)) {
            generatedNames[origin] = origin.name
            return origin.name
        }

        val homonym = generatedNames.filterValues { it == origin.name }.keys.first()
        val newParams = origin.parameters - homonym.parameters
        val newName = origin.name + newParams.firstOrNull()?.name?.capitalize()
        if (!generatedNames.containsValue(newName)) {
            generatedNames[origin] = newName
            return newName
        }

        TODO()
    }
}