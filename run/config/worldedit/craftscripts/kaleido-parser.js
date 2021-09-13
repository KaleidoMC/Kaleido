// $Id$
/*
 * Kaleido Parser Injecting CraftScript for WorldEdit
 * Copyright (C) 2021 Snownee
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

importPackage(Packages.com.sk89q.jnbt)
importPackage(Packages.com.sk89q.worldedit.util)

let we = com.sk89q.worldedit.WorldEdit.getInstance()
let factory = we.getBlockFactory()
/*
let parser = new JavaAdapter(com.sk89q.worldedit.extension.factory.parser.DefaultBlockParser, {
	parseFromInput: function(input, context) {
		return null
	}
}, we)
*/

let file = context.getSafeOpenFile("craftscripts", "kaleido-mappings", "json")
if (!file.exists())
	context.exit()

let mappings = JSON.parse( org.apache.commons.io.FileUtils.readFileToString(file) )
context.print("Kaleido parser registered!")

context.print(getBlock(argv[1], context))

function getBlock(input, ctx) {
	if (input == "hand")
		input = getIdFromItem(input, ctx.getPlayer().getItemInHand(HandSide.MAIN_HAND))
	else if (input == "offhand")
		input = getIdFromItem(input, ctx.getPlayer().getItemInHand(HandSide.OFF_HAND))

	let value = mappings[input]

	if (value === undefined)
		return context.getBlock(input)

	if (value == "")
		value = "kaleido:stuff"
	else
		value = "kaleido:" + value

	let block = ctx.getBlock(value)
	nbt = CompoundTagBuilder.create()
	nbt.putString("Model", input)
	block = block.toBaseBlock(nbt.build())
	return block
}

function getIdFromItem(input, item) {
	if (item.getType().getId() != "kaleido:stuff" || !item.hasNbtData())
		return input
	return item.getNbtData().getValue().get("Kaleido").getString("Id")
}
