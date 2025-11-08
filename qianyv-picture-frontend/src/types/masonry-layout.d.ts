declare module 'masonry-layout' {
  interface MasonryOptions {
    itemSelector?: string
    columnWidth?: number | string
    gutter?: number | string
    percentPosition?: boolean
    stamp?: string
    fitWidth?: boolean
    originLeft?: boolean
    originTop?: boolean
    containerStyle?: Record<string, string>
    transitionDuration?: string
    stagger?: number | string
    resize?: boolean
    initLayout?: boolean
  }

  class Masonry {
    constructor(element: Element | string, options?: MasonryOptions)
    layout(): void
    layoutItems(items: Element[], isStill?: boolean): void
    stamp(elements: Element | Element[]): void
    unstamp(elements: Element | Element[]): void
    appended(elements: Element | Element[]): void
    prepended(elements: Element | Element[]): void
    addItems(elements: Element | Element[]): void
    remove(elements: Element | Element[]): void
    reloadItems(): void
    destroy(): void
    getItemElements(): Element[]
    on(event: string, listener: (...args: any[]) => void): void
    off(event: string, listener: (...args: any[]) => void): void
    once(event: string, listener: (...args: any[]) => void): void
  }

  export = Masonry
}
