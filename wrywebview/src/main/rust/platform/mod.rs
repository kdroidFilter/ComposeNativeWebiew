//! Platform-specific implementations.

#[cfg(target_os = "linux")]
pub mod linux;

#[cfg(target_os = "macos")]
pub mod macos;

#[cfg(target_os = "windows")]
pub mod windows;

#[cfg(all(not(target_os = "macos"), not(target_os = "linux")))]
use crate::error::WebViewError;

#[cfg(target_os = "macos")]
pub use macos::run_on_main_thread;

/// Runs a closure on the main thread (no-op on non-macOS platforms).
#[cfg(all(not(target_os = "macos"), not(target_os = "linux")))]
pub fn run_on_main_thread<F, R>(f: F) -> Result<R, WebViewError>
where
    F: FnOnce() -> Result<R, WebViewError>,
{
    f()
}
