import { CharPage } from './app.po';

describe('char App', () => {
  let page: CharPage;

  beforeEach(() => {
    page = new CharPage();
  });

  it('should display message saying app works', () => {
    page.navigateTo();
    expect(page.getParagraphText()).toEqual('app works!');
  });
});
