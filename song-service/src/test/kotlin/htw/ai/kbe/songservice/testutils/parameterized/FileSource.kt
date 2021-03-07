package htw.ai.kbe.songservice.testutils.parameterized

import org.junit.jupiter.params.provider.ArgumentsSource

/**
@author Joscha Seelig <jduesentrieb> 2021
 **/
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
@ArgumentsSource(ResourceFileArgumentsProvider::class)
annotation class FileSource(
        /**
         * Array of file names.
         * Files must be located on classpath.
         */
        val value: Array<String>
)
