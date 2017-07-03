package blusunrize.immersiveengineering.common.util.compat.crafttweaker;

import blusunrize.immersiveengineering.api.crafting.BlastFurnaceRecipe;
import blusunrize.immersiveengineering.api.crafting.BlastFurnaceRecipe.BlastFurnaceFuel;
import blusunrize.immersiveengineering.common.util.compat.IECompatModule;
import minetweaker.IUndoableAction;
import minetweaker.CraftTweakerAPI;
import minetweaker.api.item.IIngredient;
import minetweaker.api.item.IItemStack;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.Iterator;
import java.util.List;

@ZenClass("mods.immersiveengineering.BlastFurnace")
public class BlastFurnace
{
	@ZenMethod
	public static void addRecipe(IItemStack output, IIngredient input, int time, @Optional IItemStack slag)
	{
		Object oInput = CraftTweakerHelper.toObject(input);
		if(oInput == null)
			return;

		BlastFurnaceRecipe r = new BlastFurnaceRecipe(CraftTweakerHelper.toStack(output), oInput, time, CraftTweakerHelper.toStack(slag));
		CraftTweakerAPI.apply(new Add(r));
	}

	private static class Add implements IUndoableAction
	{
		private final BlastFurnaceRecipe recipe;

		public Add(BlastFurnaceRecipe recipe)
		{
			this.recipe = recipe;
		}

		@Override
		public void apply()
		{
			BlastFurnaceRecipe.recipeList.add(recipe);
			IECompatModule.jeiAddFunc.accept(recipe);
		}

		@Override
		public boolean canUndo()
		{
			return true;
		}

		@Override
		public void undo()
		{
			BlastFurnaceRecipe.recipeList.remove(recipe);
			IECompatModule.jeiRemoveFunc.accept(recipe);
		}

		@Override
		public String describe()
		{
			return "Adding Blast Furnace Recipe for " + recipe.output.getDisplayName();
		}

		@Override
		public String describeUndo()
		{
			return "Removing Blast Furnace Recipe for " + recipe.output.getDisplayName();
		}

		@Override
		public Object getOverrideKey()
		{
			return null;
		}
	}

	@ZenMethod
	public static void removeRecipe(IItemStack output)
	{
		CraftTweakerAPI.apply(new Remove(CraftTweakerHelper.toStack(output)));
	}

	private static class Remove implements IUndoableAction
	{
		private final ItemStack output;
		List<BlastFurnaceRecipe> removedRecipes;

		public Remove(ItemStack output)
		{
			this.output = output;
		}

		@Override
		public void apply()
		{
			removedRecipes = BlastFurnaceRecipe.removeRecipes(output);
			for(BlastFurnaceRecipe recipe : removedRecipes)
				IECompatModule.jeiRemoveFunc.accept(recipe);
		}

		@Override
		public void undo()
		{
			if(removedRecipes != null)
				for(BlastFurnaceRecipe recipe : removedRecipes)
					if(recipe != null)
					{
						BlastFurnaceRecipe.recipeList.add(recipe);
						IECompatModule.jeiAddFunc.accept(recipe);
					}
		}

		@Override
		public String describe()
		{
			return "Removing Blast Furnace Recipe for " + output.getDisplayName();
		}

		@Override
		public String describeUndo()
		{
			return "Re-Adding Blast Furnace Recipe for " + output.getDisplayName();
		}

		@Override
		public Object getOverrideKey()
		{
			return null;
		}

		@Override
		public boolean canUndo()
		{
			return true;
		}
	}


	@ZenMethod
	public static void addFuel(IIngredient input, int time)
	{
		Object oInput = CraftTweakerHelper.toObject(input);
		if(oInput == null)
			return;

		CraftTweakerAPI.apply(new AddFuel(oInput, time));
	}

	private static class AddFuel implements IUndoableAction
	{
		private final Object fuel;
		private Object fuelRecipeKey;
		private final int burnTime;

		public AddFuel(Object fuel, int burnTime)
		{
			this.fuel = fuel;
			this.burnTime = burnTime;
		}

		@Override
		public void apply()
		{
			fuelRecipeKey = BlastFurnaceRecipe.addBlastFuel(fuel, burnTime);
			IECompatModule.jeiAddFunc.accept(fuelRecipeKey);
		}

		@Override
		public boolean canUndo()
		{
			return true;
		}

		@Override
		public void undo()
		{
			BlastFurnaceRecipe.blastFuels.remove(fuelRecipeKey);
			IECompatModule.jeiRemoveFunc.accept(fuelRecipeKey);
		}

		@Override
		public String describe()
		{
			return "Adding " + (fuel instanceof ItemStack ? ((ItemStack)fuel).getDisplayName() : (String)fuel) + " as Blast Furnace Fuel";
		}

		@Override
		public String describeUndo()
		{
			return "Removing " + (fuel instanceof ItemStack ? ((ItemStack)fuel).getDisplayName() : (String)fuel) + " as Blast Furnace Fuel";
		}

		@Override
		public Object getOverrideKey()
		{
			return null;
		}
	}

	@ZenMethod
	public static void removeFuel(IItemStack output)
	{
		CraftTweakerAPI.apply(new RemoveFuel(CraftTweakerHelper.toStack(output)));
	}

	private static class RemoveFuel implements IUndoableAction
	{
		private final ItemStack stack;
		BlastFurnaceFuel removed;

		public RemoveFuel(ItemStack fuel)
		{
			this.stack = fuel;
		}

		@Override
		public void apply()
		{
			Iterator<BlastFurnaceFuel> it = BlastFurnaceRecipe.blastFuels.iterator();
			while(it.hasNext())
			{
				BlastFurnaceFuel e = it.next();
				if(e.input.matchesItemStack(stack))
				{
					removed = e;
					IECompatModule.jeiRemoveFunc.accept(removed);
					it.remove();
					break;
				}
			}
		}

		@Override
		public void undo()
		{
			if(removed != null)
			{
				BlastFurnaceRecipe.blastFuels.add(removed);
				IECompatModule.jeiAddFunc.accept(removed);
			}
		}

		@Override
		public String describe()
		{
			return "Removing " + stack + " as Blast Furnace Fuel";
		}

		@Override
		public String describeUndo()
		{
			return "Re-Adding " + stack + " as Blast Furnace Fuel";
		}

		@Override
		public Object getOverrideKey()
		{
			return null;
		}

		@Override
		public boolean canUndo()
		{
			return true;
		}
	}
}
