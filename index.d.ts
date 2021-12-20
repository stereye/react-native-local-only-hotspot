declare namespace LocalOnlyHotspot {
  function start(): void;
  function stop(): void;
  function getConfig(
    onData: (config: { ssid: string; secret: string }) => void
  ): void;
}

export default LocalOnlyHotspot;
