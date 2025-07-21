package utils

import (
	"path/filepath"
	"runtime"
)

func BuildPathFromProjectRoot(relPath string) string {
	_, filename, _, _ := runtime.Caller(0)

	projectRoot := filepath.Join(filepath.Dir(filename), "..", "..", "..")
	absPath := filepath.Join(projectRoot, relPath)

	return filepath.Clean(absPath)
}
