package htw.ai.kbe.songservice.testutils.parameterized

import io.micrometer.core.instrument.util.IOUtils
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.support.AnnotationConsumer
import java.io.IOException
import java.util.stream.Stream

/**
@author Joscha Seelig <jduesentrieb> 2021
 **/
class ResourceFileArgumentsProvider
    : ArgumentsProvider, AnnotationConsumer<FileSource> {
    private lateinit var resourceNames: List<String>
    private lateinit var testClass: Class<*>

    override fun accept(fileSource: FileSource) {
        resourceNames = fileSource.value.toList()
    }

    override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
        testClass = context!!.testClass.orElseThrow()
        return resourceNames.stream()
                .map(this::readFile)
                .map(Arguments::of)
    }

    private fun readFile(resourceName: String): String {
        try {
            return testClass.getResourceAsStream(resourceName).use {
                IOUtils.toString(it, Charsets.UTF_8)
            }
        } catch (e: IOException) {
            throw IllegalArgumentException("Resource with name $resourceName could not be found.", e);
        }
    }
}
