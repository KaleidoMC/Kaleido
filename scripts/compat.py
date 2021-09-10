import os
import json
from pathlib import Path
import shutil

#namespace = input("Namespace: ")
namespace = "cocricotmod"

try:
    shutil.rmtree("./data")
    shutil.rmtree("./resources")
except OSError as e:
    print("Error: %s - %s." % (e.filename, e.strerror))
Path("./data").mkdir(parents=True, exist_ok=True)
Path("./resources").mkdir(parents=True, exist_ok=True)

json_files = [file for file in os.listdir("./models") if file.endswith(".json")]
fails = list()
skip_models = { "block/stairs", "block/inner_stairs", "block/outer_stairs", "block/half_slab",
                "block/upper_slab", "cocricotmod:block/base_vslab", "cocricotmod:block/base_plate",
                "cocricotmod:block/base_stick", "cocricotmod:block/base_rod", "cocricotmod:block/awning_stairs" }
block_models = { "cocricotmod:block/base_cube_topside" }
cutout_models = { "cocricotmod:block/climbing_rose", "block/cross", "cocricotmod:block/wall_ornament", "cocricotmod:block/base_cube_topside_nobottom",
                  "cocricotmod:block/base_nothickness_all", "cocricotmod:block/base_nothickness_both", "cocricotmod:block/base_nothickness_side",
                  "cocricotmod:block/base_nothickness_slant", "cocricotmod:block/base_nothickness_three", "cocricotmod:block/awning_black_lower", "cocricotmod:block/awning_black",
                  "cocricotmod:block/base_cross_double", "cocricotmod:block/clothes_polehanger_dark", "cocricotmod:block/rug_jute_round_true",
                  "cocricotmod:block/rug_jute_round_true_corner", "cocricotmod:block/rug_jute_round_true_side" }
cutout_prefixes = [ "parkbench_", "wheel_", "laundrypole_", "neon_", "roundtable_" ]

for filename in json_files:
    print("Process " + filename)
    if filename.startswith("base_"):
        print("Skip template model")
        continue
    try:
        with open(os.path.join("./models", filename)) as json_file:
            json_text = json.load(json_file)
            solid = False
            glass = filename.startswith("window_")
            rendertype = ""
            if glass:
                rendertype = "cutout"
            elif "leaves" in filename:
                rendertype = "cutout"  
            elif (namespace+":block/"+filename[:len(filename)-5]) in cutout_models: #TODO: 最后再检查
                rendertype = "cutout"
            
            if "parent" in json_text:
                parent = json_text["parent"]
                if parent in skip_models:
                    print("Skip " + json_text["parent"])
                    continue
                if parent in block_models:
                    solid = True
                elif parent.startswith("block/cube"):
                    solid = True
                if not solid and rendertype is "":
                    if parent in cutout_models:
                        rendertype = "cutout"
                    else:
                        for prefix in cutout_prefixes:
                            if filename.startswith(prefix):
                                rendertype = "cutout"
                                break
            elif "textures" not in json_text:
                print("Skip template model")
                continue

            with open(os.path.join("./data", filename), "w+") as f:
                data = dict()
                if glass:
                    if solid:
                        data["shape"] = "block"
                    data["glass"] = True
                    data["renderType"] = "cutout"
                elif solid:
                    data["template"] = "block"
                elif rendertype is not "":
                    data["renderType"] = rendertype
                json.dump(data, f, sort_keys=True, indent=2)
            
            with open(os.path.join("./resources", filename), "w+") as f:
                resources = dict()
                resources["parent"] = namespace + ":block/" + filename[:len(filename)-5]
                json.dump(resources, f, sort_keys=True, indent=2)
    except:
        fails.append(filename)

print("Fails:\n" + "\n".join(fails))
