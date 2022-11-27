
# Just Enough Item Descriptions
Simple JSON-based item/fluid descriptions for [JEI](https://curseforge.com/minecraft/mc-mods/jei)

# Usage

Create JSON files in the `/config/jeidescs/` folder.<br>
Make an array[] under the `entries` tag.<br>
The entry format contains an `item` or `fluid` tag specifying one or more item/fluid<br>
For the item or fluid, you can either specify a regular id or a `#tag` id<br>
You can also specify an `amount` tag for the item count or fluid amount (in mb, or 1/1000 of a bucket)<br>
Then, you specify the description to add to the given resource.<br>
`text`: A shortcut for raw text, simple as that<br>
`translate`: A shortcut for using a translation key, allowing localization of the descriptions<br>
`component`: Writing a fully functional component, just like the ones used on signs or in /tellraw. Use the `extra` tag to add multiple components to one line, with the extra components being directly added to the end of the component containing them. Do note that JEI does not support special hover or click events, so even though they can be specified they will have no effect.

# Sample Config
	{
		"entries": [
			{
				"item": "minecraft:stone",
				"text": "Simple Entry"
			},
			{
				"item": ["minecraft:stone","minecraft:dirt"],
				"text": ["Multiple item","and line","entry"]
			},
			{
				"item": "#minecraft:anvil",
				"text": "Item tags!"
			},
			{
				"item": "minecraft:diamond",
				"translate": "example.translation.key"
			},
			{
				"item": "minecraft:amethyst_shard",
				"amount": 5,
				"text": "You can also specify an amount for items"
			},
			{
				"fluid": ["minecraft:water","#minecraft:lava"],
				"text": "Works with fluids too!"
			},
			{
				"fluid": "minecraft:lava",
				"amount": 333,
				"text": "Amount works on fluids as well! (measured in mb)"
			},
			{
				"item": "minecraft:oak_sign",
				"component": {
					"text": "It works with regular chat components too!",
					"color": "#3957c4",
					"italic": true
				}
			},
			{
				"item": "minecraft:ender_pearl",
				"component": [
					{
						"text": "Also supports multiple components!"
					},
					{
						"text": "Use the extra tag to add multiple components",
						"extra": [
							{
								"text": " with different formats ",
								"underlined": true
							},
							{
								"text": "in one line!"
							}
						]
					},
					{
						"text": "Otherwise the component will move to a new line!",
						"color": "gold",
						"bold": true
					}
				]
			}
		]
	}