package net.machinemuse.api.electricity

import net.minecraft.item.ItemStack
import net.machinemuse.powersuits.common.ModCompatability
import ic2.api.item.{ElectricItem, IElectricItem}
import net.machinemuse.api.electricity.ElectricConversions._
import cofh.api.energy.IEnergyContainerItem

object ElectricAdapter {
  def wrap(stack: ItemStack): ElectricAdapter = {
    if (stack == null) return null
    stack.getItem match {
      case i: IEnergyContainerItem => new TEElectricAdapter(stack)
      case i: IElectricItem => if (ModCompatability.isIndustrialCraftLoaded) new IC2ElectricAdapter(stack) else null
      case _ => null
    }
  }

}

abstract class ElectricAdapter {
  def getCurrentEnergy: Double

  def getMaxEnergy: Double

  def drainEnergy(requested: Double): Double

  def giveEnergy(provided: Double): Double
}

class IC2ElectricAdapter(val stack: ItemStack) extends ElectricAdapter {
  val item = stack.getItem.asInstanceOf[IElectricItem]

  def getCurrentEnergy: Double = museEnergyFromEU(ElectricItem.discharge(stack, Integer.MAX_VALUE, getTier, true, true))

  def getMaxEnergy: Double = museEnergyFromEU(item.getMaxCharge(stack))

  def drainEnergy(requested: Double) = museEnergyFromEU(ElectricItem.discharge(stack, museEnergyToEU(requested).toInt, getTier, true, false))

  def giveEnergy(provided: Double): Double = museEnergyFromEU(ElectricItem.charge(stack, museEnergyToEU(provided).toInt, getTier, true, false))

  def getTier = item.getTier(stack)
}

class TEElectricAdapter(val stack: ItemStack) extends ElectricAdapter {
  val item = stack.getItem.asInstanceOf[IEnergyContainerItem]

  def getCurrentEnergy: Double = museEnergyFromRF(item.getEnergyStored(stack))

  def getMaxEnergy: Double = museEnergyFromRF(item.getMaxEnergyStored(stack))

  def drainEnergy(requested: Double): Double = museEnergyFromRF(item.extractEnergy(stack, museEnergyToRF(requested), false))

  def giveEnergy(provided: Double): Double = museEnergyFromRF(item.receiveEnergy(stack, museEnergyToRF(provided), false))

}