import SwiftUI
import KotlinModules

@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            AppRoot().ignoresSafeArea(.all)
        }
    }
}

struct AppRoot: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
