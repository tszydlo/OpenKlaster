import {EnvironmentalBenefits} from './EnvironmentalBenefits';

export class InstallationSummary {

  static totalEnergyKey = 'totalEnergy';
  static todayEnergyKey = 'energyProducedToday';
  static currentPowerKey = 'currentPower';
  static environmentalBenefitsKey = 'environmentalBenefits';
  totalEnergy: number;
  todayEnergy: number;
  currentPower: number;
  environmentalBenefits: EnvironmentalBenefits;
  power: Map<Date, number>;

  constructor() {
    this.environmentalBenefits = new EnvironmentalBenefits();
  }

  static fromDto(dtoObject: Object): InstallationSummary {
    const output = new InstallationSummary();
    output.totalEnergy = dtoObject[this.totalEnergyKey];
    output.todayEnergy = dtoObject[this.todayEnergyKey];
    output.currentPower = dtoObject[this.currentPowerKey];
    output.environmentalBenefits = EnvironmentalBenefits.fromDto(dtoObject[this.environmentalBenefitsKey]);
    return output;
  }
}
