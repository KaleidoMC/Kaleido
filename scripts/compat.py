import os
import json
from pathlib import Path
import shutil


def dumpRes(namespace, filename):
    override = Path("./override_resources/" + filename)
    if override.exists():
        if override.stat().st_size is not 0:
            shutil.copyfile(override, os.path.join("./resources", filename))
    else:
        with open(os.path.join("./resources", filename), "w+") as f:
            resources = dict()
            resources["parent"] = namespace + ":block/" + filename[:len(filename)-5]
            json.dump(resources, f, sort_keys=True, indent=2)

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

skip_prefixes = [ "base_", "door_1glass_", "door_2panel_", "door_4glass_", "door_9glass_", "door_stainless_", "door_tall_bottom", "door_tall_top", "log_all_" ]

block_models = { "cocricotmod:block/base_cube_topside", "cocricotmod:block/base_shelf", "cocricotmod:block/barrel_antique", "cocricotmod:block/base_cube_all",
                 "cocricotmod:block/basket", "cocricotmod:block/base_cube_topfrontsidebottom", "cocricotmod:block/carpet_block", "cocricotmod:block/carpet_block_fringe",
                 "cocricotmod:block/cafetable", "cocricotmod:block/base_cube_topside_nobottom"}

cutout_models = { "cocricotmod:block/climbing_rose", "block/cross", "cocricotmod:block/wall_ornament", "cocricotmod:block/base_cube_topside_nobottom",
                  "cocricotmod:block/base_nothickness_all", "cocricotmod:block/base_nothickness_both", "cocricotmod:block/base_nothickness_side",
                  "cocricotmod:block/base_nothickness_slant", "cocricotmod:block/base_nothickness_three", "cocricotmod:block/awning_black_lower", "cocricotmod:block/awning_black",
                  "cocricotmod:block/base_cross_double", "cocricotmod:block/clothes_polehanger_dark", "cocricotmod:block/rug_jute_round_true",
                  "cocricotmod:block/rug_jute_round_true_corner", "cocricotmod:block/rug_jute_round_true_side", "cocricotmod:block/drooping_rose",
                  "cocricotmod:block/base_cross_thick", "cocricotmod:block/hangerrack_clothes", "cocricotmod:block/bougainvillea", "cocricotmod:block/bathrobe",
                  "cocricotmod:block/base_cross_odd", "cocricotmod:block/bench_turnedleg_black_both", "cocricotmod:block/bench_turnedleg_black_left", "cocricotmod:block/bench_turnedleg_black_right",
                  "cocricotmod:block/bench_turnedleg_black_single", "cocricotmod:block/bicycle_black_set", "cocricotmod:block/bicycle_black_stand", "cocricotmod:block/bathtub_clawfoot",
                  "cocricotmod:block/curtain_separate_right", "cocricotmod:block/curtain_separate_left", "cocricotmod:block/curtain_separate_both", "cocricotmod:block/curtain_fringe_black",
                  "cocricotmod:block/base_pane_thin_all", "cocricotmod:block/base_pane_thin_both", "cocricotmod:block/base_pane_thin_corner", "cocricotmod:block/base_pane_thin_post",
                  "cocricotmod:block/base_pane_thin_side", "cocricotmod:block/base_pane_thin_three", "cocricotmod:block/window_pane_all", "cocricotmod:block/window_pane_both",
                  "cocricotmod:block/window_pane_corner", "cocricotmod:block/window_pane_post", "cocricotmod:block/window_pane_side", "cocricotmod:block/window_pane_three", "cocricotmod:block/window_pane_slant",
                  "cocricotmod:block/fireplace", "cocricotmod:block/flowerpot_inner", "cocricotmod:block/gardenbench_both", "cocricotmod:block/gardenbench_left",
                  "cocricotmod:block/gardenbench_right", "cocricotmod:block/gardenbench_single", "cocricotmod:block/industrialshadelamp_black_wall", "cocricotmod:block/marinelamp_bulkhead",
                  "cocricotmod:block/marinelamp_deck", "cocricotmod:block/marinelamp_lampshade", "cocricotmod:block/marinelamp_lampshade_wall", "cocricotmod:block/marketumbrella_black",
                  "cocricotmod:block/bracket_antique_base", "cocricotmod:block/rug_jute_round", "cocricotmod:block/rug_jute_round_corner", "cocricotmod:block/rug_jute_round_side",
                  "cocricotmod:block/wallshelf_bracket_antique_single", "cocricotmod:block/wallshelf_bracket_antique_single_false", "cocricotmod:block/lamppost_side",
                  "cocricotmod:block/base_placed", "cocricotmod:block/base_cross_odd_table", "cocricotmod:block/windowframe_triangle_right", "cocricotmod:block/windowframe_triangle_left",
                  "cocricotmod:block/windowframe_triangle", "cocricotmod:block/wallshelf_chain_false", "cocricotmod:block/wallshelf_chain", "cocricotmod:block/vase",
                  "cocricotmod:block/base_nothickness_post", "cocricotmod:block/coffeetable_iron_left", "cocricotmod:block/coffeetable_iron_right", "cocricotmod:block/coffeetable_iron_both",
                  "cocricotmod:block/coffeetable_iron_single", "cocricotmod:block/wallshelf", "cocricotmod:block/roof_incline_outer", "cocricotmod:block/roof_incline_odd_corner",
                  "cocricotmod:block/wallshelf_bracket_antique_right_false", "cocricotmod:block/wallshelf_bracket_antique_right",
                  "cocricotmod:block/wallshelf_bracket_antique_left_false", "cocricotmod:block/wallshelf_bracket_antique_left", "cocricotmod:block/base_placed_table"}

cutout_prefixes = [ "parkbench_", "wheel_", "laundrypole_", "roundtable_", "door_tall_1glass_", "door_tall_2glass3panel_", "door_tall_6glass_", "door_tall_8glass_", "glass_frame_",
                    "lantern_", "handrail_", "telephonebox_"]

##len = len(namespace + ":block/")
##for modelname in cutout_models:
##    filename = modelname[len:] + ".json"
##    if filename in json_files:
##        print(filename)

for filename in json_files:
    print("Process " + filename)
    try:
        override = Path("./override_data/" + filename)
        if override.exists():
            if override.stat().st_size is 0:
                print("Skip template model")
            else:
                shutil.copyfile(override, os.path.join("./data", filename))
                dumpRes(namespace, filename)
            continue

        skip = False
        for prefix in skip_prefixes:
            if filename.startswith(prefix):
                print("Skip template model " + prefix)
                skip = True
                break
        if skip:
            continue
        
        with open(os.path.join("./models", filename)) as json_file:
            json_text = json.load(json_file)
            solid = False
            glass = filename.startswith("window_")
            rendertype = ""
            if glass:
                rendertype = "cutout"
            elif "leaves" in filename:
                rendertype = "cutout"
            elif filename.startswith("neon_"):
                rendertype = "translucent"
            
            if "parent" in json_text:
                parent = json_text["parent"]
                if parent in skip_models:
                    print("Skip " + json_text["parent"])
                    continue
                if parent in block_models:
                    solid = True
                elif parent.startswith("block/cube"):
                    solid = True
                if rendertype is "":
                    if parent in cutout_models:
                        rendertype = "cutout"
            elif "textures" not in json_text:
                print("Skip template model")
                continue
            if rendertype is "":
                for prefix in cutout_prefixes:
                    if filename.startswith(prefix):
                        rendertype = "cutout"
                        break

            with open(os.path.join("./data", filename), "w+") as f:
                data = dict()
                if glass or rendertype is not "":
                    if solid:
                        data["shape"] = "block"
                    if glass:
                        data["glass"] = True
                    data["renderType"] = rendertype
                elif solid:
                    data["template"] = "block"
                elif rendertype is not "":
                    data["renderType"] = rendertype
                json.dump(data, f, sort_keys=True, indent=2)
            
            dumpRes(namespace, filename)
    except:
        fails.append(filename)

print("Fails:\n" + "\n".join(fails))
