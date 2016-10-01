(ns semver.modifiers)

(defn increment-major
  "Returns a copy of a given version with the major version icremented"
  [version]
  (update version :major inc))

(defn increment-minor
  "Returns a copy of the given version with the minor version incremented"
  [version]
  (update version :minor inc))

(defn increment-patch
  "Returns a copy of the given version with the patch version incremented"
  [version]
  (update version :patch inc))
