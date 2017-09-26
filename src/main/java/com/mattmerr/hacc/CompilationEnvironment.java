package com.mattmerr.hacc;

import static java.util.Collections.shuffle;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;

import com.mattmerr.hacc.phase.LLVMCompilationStep;
import com.mattmerr.hacc.phase.ParseStep;
import com.mattmerr.hacc.phase.RunInJITStep;
import com.mattmerr.hitch.TokenStream;
import com.mattmerr.hitch.parsetokens.ParseNodeFile;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

public class CompilationEnvironment {

  private final String[] srcPaths;

  private TreeSet<CompilationStep> compilationSteps = new TreeSet<>();
  private HashMap<String, CompiledFile> parsed = new HashMap<>();

  public CompilationEnvironment() {
    this(new String[]{"."});
  }

  public CompilationEnvironment(String[] srcPaths) {
    this.srcPaths = srcPaths;
  }

  public static Path resolveIdInPaths(String id, String[] srcPaths) {
    if (requireNonNull(id).isEmpty()) {
      throw new IllegalArgumentException("ID May not be empty");
    }

    String[] resolveParts = id.split("\\.");
    resolveParts[resolveParts.length - 1] += ".hipl";

    srcPathLoop:
    for (String srcPath : srcPaths) {
      Path path = Paths.get(srcPath);

      for (String resolvePart : resolveParts) {
        path = path.resolve(resolvePart);
        if (!Files.exists(path)) {
          continue srcPathLoop;
        }
      }

      return path;
    }
    throw new RuntimeException("Could not resolve \"" + id + "\" in " + Arrays.toString(srcPaths));
  }

  public void addFile(String id) throws IOException {
    Path path = resolveIdInPaths(id, srcPaths);
    InputStream is = Files.newInputStream(path);
    TokenStream ts = new TokenStream(is);

    ParseStep parseStep = new ParseStep(id, ts,
        (parsedBlock) -> new LLVMCompilationStep(parsedBlock));

    addCompilationStep(parseStep);
  }

  public CompiledFile getFile(String id) {
    return requireNonNull(this.parsed.get(requireNonNull(id)));
  }

  public void addCompilationStep(CompilationStep step) {
    compilationSteps.add(step);
  }

  public void runAll() {
    CompilationStep compilationStep;
    while ((compilationStep = compilationSteps.pollFirst()) != null) {
      compilationStep.run(this);
    }
  }

  public void setParsed(String id, CompiledFile file) {
    this.parsed.put(id, file);
  }

  public boolean hasBeenParsed(String id) {
    return this.parsed.containsKey(id);
  }

  public Set<String> getParsedIds() {
    return unmodifiableSet(parsed.keySet());
  }

}
