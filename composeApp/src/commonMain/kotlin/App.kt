import androidx.compose.runtime.Composable
import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor.KtorNetworkFetcherFactory
import com.santimattius.kmp.skeleton.MainApplication
import com.santimattius.kmp.skeleton.core.ui.themes.AppTheme

@OptIn(ExperimentalCoilApi::class)
@Composable
fun App() {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components {
                add(KtorNetworkFetcherFactory())
            }
            .build()
    }
    AppTheme {
        MainApplication()
    }
}