export const SPORTS = [
  { id: 1, label: 'sports.futbol', value: 'FUTBOL' },
  { id: 2, label: 'sports.billar', value: 'BILLAR' }
];

export const GAMEMODE_OPTIONS = [
  { id: 1, label: 'gamemodes.futbol11', value: 'FUTBOL_11', sportId: 1 },
  { id: 2, label: 'gamemodes.futbol8', value: 'FUTBOL_8', sportId: 1 },
  { id: 3, label: 'gamemodes.futbol7', value: 'FUTBOL_7', sportId: 1 },
  { id: 4, label: 'gamemodes.futbol5', value: 'FUTBOL_5', sportId: 1 },
  { id: 5, label: 'gamemodes.billar3', value: 'BILLAR_3', sportId: 2 }
];

export const gamemodesMap: Record<number, string[]> = GAMEMODE_OPTIONS.reduce((map, option) => {
  if (!map[option.sportId]) {
    map[option.sportId] = [];
  }
  map[option.sportId].push(option.value);
  return map;
}, {} as Record<number, string[]>);

export const sportIdToLabelMap = Object.fromEntries(
  SPORTS.map(s => [s.id, s.label])
);

export const gamemodeIdToLabelMap = Object.fromEntries(
  GAMEMODE_OPTIONS.map(g => [g.id, g.label])
);

export const getSportIdByValue = (value: string): number | undefined =>
  SPORTS.find(s => s.value === value)?.id;

