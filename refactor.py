import re

with open("app/src/main/java/com/sosinhvien/app/data/MockDataRepository.java", "r") as f:
    content = f.read()

# Add imports
content = content.replace("import java.util.regex.Pattern;", "import java.util.regex.Pattern;\nimport java.util.concurrent.ExecutorService;\nimport java.util.concurrent.Executors;\nimport android.os.Handler;\nimport android.os.Looper;")

# Add executor and handler
class_start = content.find("public final class MockDataRepository {")
insertion = """public final class MockDataRepository {

    public interface DataCallback<T> { void onDataLoaded(T data); }
    public interface ActionCallback { void onComplete(boolean success); }

    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
"""
content = content.replace("public final class MockDataRepository {", insertion)

# We have to wrap all DB accessing methods. This is complex to do automatically with regex.
# I'll just write the new file content to a new file, or I can do it manually.

with open("app/src/main/java/com/sosinhvien/app/data/MockDataRepository_new.java", "w") as f:
    f.write(content)

